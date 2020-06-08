package algorithm;

import java.util.HashMap;
import java.util.List;

public class MultinomialFairRanker {
    
    public enum FairRankingStrategy {
        
        MOST_LIKELY,
        MOST_UNLIKELY,
        RANDOM;
    }

    private MTree mTree;

    public MultinomialFairRanker(int k, double[] p, double alpha) {
        this.mTree = new MTree(k,p,alpha);
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
        
        for(int k=0; k < length; k++) {
            
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
