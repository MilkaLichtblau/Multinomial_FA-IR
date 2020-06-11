package algorithm;

import java.io.Serializable;
import java.util.*;

public class FailProbabilityEstimator implements Serializable {

    public static int RUNS_FOR_FAILPROB = 10000;
    private MTree mTree;
    private Double failProbability;

    public FailProbabilityEstimator(MTree mTree) {
        this.mTree = mTree;
    }


    private ArrayList<Integer> createRanking(int k, double[] cumulativeProportions) {
        ArrayList<Integer> ranking = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < k; i++) {
            double r = random.nextDouble();
            for (int j = 0; j < cumulativeProportions.length; j++) {
                if (r <= cumulativeProportions[j]) {
                    ranking.add(j);
                    break;
                }
            }
        }
        return ranking;
    }

    private boolean testWithLazyMTree(ArrayList<Integer> ranking, HashMap<ArrayList<Integer>, Boolean> testCache) {
        if (testCache.containsKey(ranking)) {
            return testCache.get(ranking);
        }
        int[] seenSoFar = new int[this.mTree.getP().length];
        for (int i = 0; i < ranking.size(); i++) {
            seenSoFar[ranking.get(i)]++;
            HashSet<List<Integer>> nodes = this.mTree.getTree().get(i);
            int enoughProtectedCount = 0;
            for (List<Integer> t : nodes) {
                if (enoughProtected(t, seenSoFar)) {
                    enoughProtectedCount++;
                    break;
                }
            }
            if (enoughProtectedCount == 0) {
                testCache.put(ranking, false);
                return false;
            }
        }
        testCache.put(ranking, true);
        return true;
    }

    private boolean enoughProtected(List<Integer> node, int[] seenSoFar) {
        if (this.mTree.isMinimumProportionsSymmetric()) {
            boolean mirror1 = true;
            boolean mirror2 = true;
            List<Integer> mirrorNode = MTree.mirror(node);
            for (int i = 1; i < seenSoFar.length; i++) {
                if (node.get(i) > seenSoFar[i]) {
                    mirror1 = false;
                }
                if (mirrorNode.get(i) > seenSoFar[i]) {
                    mirror2 = false;
                }
            }
            return mirror1 || mirror2;
        } else {
            for (int i = 1; i < seenSoFar.length; i++) {
                if (node.get(i) > seenSoFar[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    public Double getFailProbability() {
        if (this.failProbability == null) {
            double[] p = this.mTree.getP();
            int k = this.mTree.getK();
            int successes = 0;
            HashMap<ArrayList<Integer>, Boolean> testCache = new HashMap<>();
            double[] cumulativeProportions = new double[p.length];
            cumulativeProportions[0] = p[0];
            for (int i = 1; i < p.length; i++) {
                cumulativeProportions[i] = p[i] + cumulativeProportions[i - 1];
            }
            for (int i = 0; i < RUNS_FOR_FAILPROB; i++) {
                ArrayList<Integer> ranking = createRanking(k, cumulativeProportions);
                boolean test = testWithLazyMTree(ranking, testCache);
                if (test) {
                    successes++;
                }
            }
            this.failProbability = 1 - (double) successes / (double) RUNS_FOR_FAILPROB;
        }
        return this.failProbability;
    }
}
