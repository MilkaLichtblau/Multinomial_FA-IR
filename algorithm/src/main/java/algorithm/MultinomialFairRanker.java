package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MultinomialFairRanker {

    public enum FairRankingStrategy {
        /**
         * enum to choose from different ranking strategies --
         *        MOST_LIKELY = child node with highest mcdf 
         *        MOST_UNLIKELY = child with lowest mcdf (still valid though) 
         *        RANDOM = pick random child node
         */
        MOST_LIKELY, MOST_UNLIKELY, RANDOM;
    }

    private MTree mTree;
    private HashMap<Integer, List<Candidate>> groupLists;
    private HashMap<Integer, Integer> numberOfCandidatesInResultPerGroup;

    public MultinomialFairRanker(int k, double[] p, double alpha, boolean doAdjust, List<Candidate> unfairRanking) {
        this.mTree = new MTree(k, p, alpha, doAdjust, new MCDFCache(p));

        // create separate lists of candidates per group, with candidates sorted by
        // score
        this.groupLists = new HashMap<>();
        for (int groupID = 0; groupID < p.length; groupID++) {
            List<Candidate> groupList = new ArrayList<>();
            for (Candidate candidate : unfairRanking) {
                if (candidate.getGroup() == groupID) {
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
     * @param groupLists: contains all available candidates for the ranking sorted
     *        into separate lists per group. Hashmap with group id as key and list
     *        of candidates of this group as value. Candidates are sorted by
     *        decreasing scores
     * @param strategy: enum to choose from different ranking strategies --
     *        MOST_LIKELY = child node with highest mcdf MOST_UNLIKELY = child with
     *        lowest mcdf (still valid though) RANDOM = pick random child node
     * @param length: length of the output ranking
     * @return
     */
    public List<Candidate> buildFairRanking(FairRankingStrategy strategy, Integer length) {
        // FIXME: Write code for asymmetric case
        this.mTree.isMinimumProportionsSymmetric();
        List<Candidate> result = new ArrayList<>(Arrays.asList(new Candidate[length]));

        for (int k = 0; k < length; k++) {
            // check how many candidates of each group is needed at least at current k
            // as their may be different possibilities, take the one with the according
            // strategy
            // if the tree is symmetric, the selected node has a mirror with the exact same
            // mcdf and
            List<Integer> mNode = getTheNode(strategy, k+1);
            // is therefore equally likely to be selected. Pick one of these at random
            boolean candidateAdded = false;
            // start from index 1, because first entry in node is current position
            for (int groupID = 1; groupID < mNode.size(); groupID++) {
                Integer expected = mNode.get(groupID);
                Integer actual = this.numberOfCandidatesInResultPerGroup.get(groupID);
                if (expected > actual) {
                    // check if multinomial ranked group fairness criteria is met,
                    // if not add candidate from group where is missing
                    // always use first candidate, because this is the best in terms of scores
                    try {
                        Candidate candidate = groupLists.get(groupID).get(0);
                        result.set(k, candidate);
                        candidateAdded = true;
                        this.numberOfCandidatesInResultPerGroup.put(groupID, actual+1);
                        // do not put the same candidate twice, therefore remove from pool
                        groupLists.get(groupID).remove(0);
                        // no need to add candidates twice at the same position
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Not enough candidates in group " + groupID
                                + " to create ranking with parameters k=" + this.mTree.getK() + ", p="
                                + this.mTree.getP() + ", and alpha=" + this.mTree.getAlpha());
                    }
                }
            }
            if (!candidateAdded) {
                // if ranked group fairness condition is met, add the candidate with the highest
                // score
                // FIXME: What to do if one list is already empty?
                Candidate bestCandidate = groupLists.get(0).get(0);
                for (int groupID = 1; groupID < mNode.size(); groupID++) {
                    if (bestCandidate.getScore() < groupLists.get(groupID).get(0).getScore()) {
                        bestCandidate = groupLists.get(groupID).get(0);
                    }
                }
                result.set(k, bestCandidate);
                groupLists.get(bestCandidate.getGroup()).remove(0);

            }

        }

        return null;
    }

    protected List<Integer> getTheNode(FairRankingStrategy strategy, int currentPosition) {
        /**
         * from all possible mNodes at this layer, returns the node that fits the
         * defined strategy
         * 
         * @param strategy: enum to choose from different ranking strategies --
         *        MOST_LIKELY = child node with highest mcdf 
         *        MOST_UNLIKELY = child with lowest mcdf (still valid though) 
         *        RANDOM = pick random child node
         */
        HashSet<List<Integer>> mNodesAtPosition = this.mTree.getAllNodesOfLevel(currentPosition);
        MCDFCache mcdfCache = this.mTree.getMcdfCache();

        // get any node of this layer in the mTree to initialize
        List<Integer> result = mNodesAtPosition.iterator().next();

        switch (strategy) {
        case MOST_LIKELY:
            Double highestMCDF = 0.0;
            // find the likeliest node
            for (List<Integer> mNode : mNodesAtPosition) {
                if (mcdfCache.mcdf(mNode) > highestMCDF) {
                    result = mNode;
                }
            }
            break;
        case MOST_UNLIKELY:
            Double lowestMCDF = 1.0;
            // find the unlikeliest node
            for (List<Integer> mNode : mNodesAtPosition) {
                if (mcdfCache.mcdf(mNode) < lowestMCDF) {
                    result = mNode;
                }
            }
            break;
        case RANDOM:
            // from all elements in set, pick one at random from uniform distributed
            // randomness
            int randomIndex = new Random().nextInt(mNodesAtPosition.size());
            Iterator<List<Integer>> iter = mNodesAtPosition.iterator();
            for (int i = 0; i < randomIndex; i++) {
                iter.next();
            }
            result = iter.next();
            break;
        default:
            throw new IllegalArgumentException("strategy must be either MOST_LIKELY, MOST_UNLIKELY or RANDOM");
        }
        return result;
    }
}
