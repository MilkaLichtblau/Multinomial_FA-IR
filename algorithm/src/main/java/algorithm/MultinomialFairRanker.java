package algorithm;

import java.util.List;

public class MultinomialFairRanker {

    private int k;
    private double[] p;
    private double alpha;
    private MTree mTree;

    public MultinomialFairRanker(int k, double[] p, double alpha) {
        this.k = k;
        this.p = p;
        this.alpha = alpha;
        //this.mTree = new MTree(k,p,alpha);
    }

    public List<Candidate> buildFairRanking(List<List<Candidate>> groupLists) {

        return null;
    }
}
