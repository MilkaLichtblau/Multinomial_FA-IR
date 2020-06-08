package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MultinomialFairRanker {
    
    public enum FairRankingStrategy {
        
        MOST_LIKELY,
        MOST_UNLIKELY,
        RANDOM;
    }

    private MTree mTree;
    private HashMap<Integer, List<Candidate>> groupLists;
    private HashMap<Integer, Integer> numberOfCandidatesInResultPerGroup;

    public MultinomialFairRanker(int k, double[] p, double alpha, boolean doAdjust, List<Candidate> unfairRanking) {
        this.mTree = new MTree(k,p,alpha, doAdjust, new MCDFCache(p));
        
        //create separate lists of candidates per group, with candidates sorted by score
        this.groupLists = new HashMap<>();
        for(int groupID=0; groupID < p.length; groupID++) {
            List<Candidate> groupList = new ArrayList<>();
            for (Candidate candidate : unfairRanking) {
                if(candidate.getGroup() == groupID) {
                    groupList.add(candidate);
                }
            }
            Collections.sort(groupList);
            this.groupLists.put(groupID, groupList);
        }
        
        this.numberOfCandidatesInResultPerGroup = new HashMap<>();
        for (Integer key : groupLists.keySet()) {
            this.numberOfCandidatesInResultPerGroup.put(key, 0);
        }
    }

    /**
     * @param groupLists:   contains all available candidates for the ranking sorted into separate 
     *                      lists per group. Hashmap with group id as key and list of candidates of 
     *                      this group as value. Candidates are sorted by decreasing scores
     * @param strategy:     enum to choose from different ranking strategies -- 
     *                          MOST_LIKELY = child node with highest mcdf
     *                          MOST_UNLIKELY = child with lowest mcdf (still valid though)
     *                          RANDOM = pick random child node
     * @param length:       length of the output ranking
     * @return
     */
    public List<Candidate> buildFairRanking(FairRankingStrategy strategy, Integer length) {
        
        this.mTree.isMinimumProportionsSymmetric();
        MCDFCache mcdfCache = this.mTree.getMcdfCache();
        List<Candidate> result = new ArrayList<>();

        for(int k=0; k < length; k++) {
            //check how many candidates of each group is needed at least at current k
            // as their may be different possibilities, take the one with the according strategy
            // if the tree is symmetric, the selected node has a mirror with the exact same mcdf and 
            // is therefore equally likely to be selected. Pick one of these at random
            HashSet<List<Integer>> mNodesAtPosition = this.mTree.getAllNodesOfLevel(k);
            switch (strategy) {
            case MOST_LIKELY:
                Double highestMCDF = 0.0;
                //get any element from set to initialize
                List<Integer> nodeWithHighestMCDF = mNodesAtPosition.iterator().next();
                //find the likeliest node
                for (List<Integer> mNode : mNodesAtPosition) {
                    if (mcdfCache.mcdf(mNode) > highestMCDF) {
                        nodeWithHighestMCDF = mNode;
                    }
                }
                boolean candidateAdded = false;
                //start from index 1, because first entry in node is current position
                for (int groupID=1; groupID < nodeWithHighestMCDF.size(); groupID++) {
                    Integer expected = nodeWithHighestMCDF.get(groupID);
                    Integer actual = this.numberOfCandidatesInResultPerGroup.get(groupID);
                    if(expected > actual) {
                        //check if multinomial ranked group fairness criteria is met, 
                        //if not add candidate from group where is missing
                        //always use first candidate, because this is the best in terms of scores
                        try {
                            Candidate candidate = groupLists.get(groupID).get(0);
                            result.add(k, candidate);
                            candidateAdded = true;
                            //do not put the same candidate twice, therefore remove from pool
                            groupLists.get(groupID).remove(0);
                            //no need to add candidates twice at the same position
                            break;
                        }catch (IndexOutOfBoundsException e) {
                            throw new IllegalArgumentException("Not enough candidates in group " + 
                                                               groupID + " to create ranking with " +
                                                               "parameters k=" + this.mTree.getK() + 
                                                               ", p=" + this.mTree.getP() + ", and" +
                                                               ", alpha=" + this.mTree.getAlpha());
                        }
                    }                    
                }
                if(!candidateAdded) {
                    //if ranked group fairness condition is met, add the candidate with the highest score
                    Candidate bestCandidate = groupLists.get(0).get(0); 
                    for (int groupID = 1; groupID < nodeWithHighestMCDF.size(); groupID++) {
                        if(bestCandidate.getScore() < groupLists.get(groupID).get(0).getScore()) {
                            bestCandidate = groupLists.get(groupID).get(0);
                        }
                    }
                    result.add(k, bestCandidate);
                }
                break;
            case MOST_UNLIKELY:
                break;
            case RANDOM:
                break;
            default:
                throw new IllegalArgumentException("strategy must be either MOST_LIKELY, MOST_UNLIKELY or RANDOM");
            }
        }
        
        return null;
    }
}
