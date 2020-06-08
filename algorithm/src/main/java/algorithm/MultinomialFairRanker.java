package algorithm;

import java.util.ArrayList;
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

    public MultinomialFairRanker(int k, double[] p, double alpha, boolean doAdjust) {
        this.mTree = new MTree(k,p,alpha, doAdjust, new MCDFCache(p));
    }

    /**
     * @param groupLists:   contains all available candidates for the ranking sorted into separate 
     *                      lists per group. Hashmap with group id as key and list of candidates of 
     *                      this group as value
     * @param strategy:     enum to choose from different ranking strategies -- 
     *                          MOST_LIKELY = child node with highest mcdf
     *                          MOST_UNLIKELY = child with lowest mcdf (still valid though)
     *                          RANDOM = pick random child node
     * @param length:       length of the output ranking
     * @return
     */
    public List<Candidate> buildFairRanking(HashMap<Integer, List<Candidate>> groupLists, FairRankingStrategy strategy, Integer length) {
        
        this.mTree.isMinimumProportionsSymmetric();
        MCDFCache mcdfCache = this.mTree.getMcdfCache();
        List<Candidate> result = new ArrayList<>();

        for(int k=0; k < length; k++) {
            //check how many candidates of each group is needed at least at current k
            // as their may be different possibilities, take the one with the according strategy
            // if the tree is symmetric, the selected node has a mirror with the exact same mcdf and 
            // is therefore equally likely to be selected. Pick one of these at random
            HashSet<List<Integer>> mNodesAtPosition = this.mTree.getAllNodesOfLevel(k);
            Double highestMCDF = 0.0; 
            for (List<Integer> mNode : mNodesAtPosition) {
                nodeWithHighestMCDF = 
            }
        }
        
        switch (strategy) {
        case MOST_LIKELY:
            
            break;
        case MOST_UNLIKELY:
            break;
        case RANDOM:
            break;
        default:
            throw new IllegalArgumentException("strategy must be either MOST_LIKELY, MOST_UNLIKELY or RANDOM");
        }
        return null;
    }
}
