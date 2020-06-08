package algorithm;

import java.util.*;

public class MTree {

    public static int RUNS_FOR_FAILPROB = 10000;
    public static double EPS = 0.001;

    private double alpha;
    public double unadjustedAlpha;
    private double[] p;
    private int k;
    /**
     * Stores the minimum number of protected candidates needed at each position for all protected groups.
     * The inner HashSet represents all valid nodes of a particular level in the mTree without duplicates and without mirrors.
     */
    private HashMap<Integer, HashSet<List<Integer>>> tree; //FIXME: write comment about how this data structure looks like
    private HashMap<List<Integer>, Integer> nodeWeights;
    private boolean doAdjust;
    private MCDFCache mcdfCache;
    private boolean isMinimumProportionsSymmetric;
    private Double failprob;


    public MTree(int k, double[] p, double alpha, boolean doAdjust, MCDFCache mcdfCache) {
        this.k = k;
        this.p = p;
        this.alpha = alpha;
        this.unadjustedAlpha = alpha;
        this.doAdjust = doAdjust;
        this.mcdfCache = mcdfCache;
        this.nodeWeights = new HashMap<>();

        //check if we have symmetric proportions p[] to allow later optimizations
        this.isMinimumProportionsSymmetric = true;
        if (p.length <= 2) {
            // we only have one protected group
            this.isMinimumProportionsSymmetric = false;
        } else {
            for (int i = 1; i < p.length; i++) {
                for (int j = 1; j < p.length; j++) {
                    if (p[i] != p[j]) {
                        this.isMinimumProportionsSymmetric = false;
                    }
                }
            }
        }

        //check if Alpha Adjustment shall be used
        if (doAdjust) {
            this.tree = this.buildAdjustedMTree();
        } else {
            this.tree = this.buildMTree();
        }
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildAdjustedMTree() {
        double aMin = 0;
        double aMax = this.alpha;
        double aMid = (aMin + aMax) / 2.0;

        MTree max = new MTree(this.k, this.p, aMax, false, this.mcdfCache);
        if (max.getFailprob() == 0) {
            return max.tree;
        }
        MTree min = new MTree(k, p, aMin, false, mcdfCache);
        MTree mid = new MTree(k, p, aMid, false, mcdfCache);

        if (Math.abs(max.getFailprob() - this.alpha) <= EPS) {
            return max.tree;
        }
        while (true) {
            if(mid.getFailprob() == this.alpha){
                return mid.tree;
            }
            if (mid.getFailprob() < this.alpha) {
                aMin = aMid;
                min = new MTree(k, p, aMin,false, mcdfCache);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid,false, mcdfCache);
            } else if (mid.getFailprob() > this.alpha) {
                aMax = aMid;
                max = new MTree(k, p, aMax,false, mcdfCache);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid,false, mcdfCache);
            }

            double midDiff = Math.abs(mid.getFailprob() - this.alpha);
            double maxDiff = Math.abs(max.getFailprob() - this.alpha);
            double minDiff = Math.abs(min.getFailprob() - this.alpha);
            if (midDiff <= EPS && (midDiff <= maxDiff && midDiff <= minDiff)) {
                return mid.tree;
            }
            if (minDiff <= EPS && (minDiff <= maxDiff && minDiff <= midDiff)) {
                return min.tree;
            }
            if (maxDiff <= EPS && maxDiff <= minDiff && maxDiff <= midDiff) {
                return max.tree;
            }
        }
    }

    private double getFailprob() {
        //TODO: Implement with experimental failprob calculation
        if (this.failprob == null) {
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
            this.failprob = 1 - (double) successes / (double) RUNS_FOR_FAILPROB;
        }
        return this.failprob;
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
        int[] seenSoFar = new int[p.length];
        for (int i = 0; i < ranking.size(); i++) {
            seenSoFar[ranking.get(i)]++;
            HashSet<List<Integer>> nodes = this.tree.get(i);
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
        if (this.isMinimumProportionsSymmetric) {
            boolean mirror1 = true;
            boolean mirror2 = true;
            List<Integer> mirrorNode = this.mirror(node);
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

    private HashMap<Integer, HashSet<List<Integer>>> buildMTree() {
        HashMap<Integer, HashSet<List<Integer>>> tree = new HashMap<>();
        int position = 0;

        //Create root node and fill it with zero entries
        //we need the root node for initialisation it represents ranking position 0 which is no real position
        List<Integer> root = new ArrayList<>(Arrays.asList(new Integer[this.p.length]));
        Collections.fill(root, 0);
        HashSet<List<Integer>> positionZero = new HashSet<>();
        positionZero.add(root);
        tree.put(position, positionZero);

        while (position < this.k) {
            HashSet<List<Integer>> currentLevel = tree.get(position);
            HashSet<List<Integer>> currentChildCandidates = new HashSet<>();
            for (List<Integer> node : currentLevel) {
                currentChildCandidates.addAll(inverseMultinomialCDF(node));
            }
            //if minimum proportions in p[] are equal, we can delete many "mirrored" nodes and thus be more efficient
            if (this.isMinimumProportionsSymmetric) {
                currentChildCandidates = removeMirroredNodes(currentChildCandidates);
            }
            position++;
            tree.put(position, currentChildCandidates);
        }
        tree.remove(0);
        return tree;
    }

    public HashSet<List<Integer>> removeMirroredNodes(HashSet<List<Integer>> currentChildCandidates) {
        // [1,1,1] ----> darf sich nicht selbst löschen
        /// 1. Nehme einen Knoten aus dem set
        // 2. Copy and Mirror den Knoten
        // 3. Gucke ob Mirror = Knoten ? Ja --> nächster Knoten Nein? ---> 4.
        //4. Gibt es den im Set?

        for (List<Integer> node : currentChildCandidates) {
            List<Integer> mirror = mirror(node);
            if (!mirror.equals(node)) {
                if (currentChildCandidates.contains(mirror)) {
                    currentChildCandidates.remove(mirror);
                    break;
                }
            }
        }
        return currentChildCandidates;
    }

    private List<Integer> mirror(List<Integer> node) {
        List<Integer> node_sublist = node.subList(1, node.size());
        List<Integer> mirror = new ArrayList<>();
        for (Integer m_i : node_sublist) {
            mirror.add(0, m_i);
        }
        mirror.add(0, node.get(0));
        return mirror;
    }

    private HashSet<List<Integer>> inverseMultinomialCDF(List<Integer> node) {
        List<Integer> childNode = new ArrayList<>(List.copyOf(node));
        childNode.set(0, childNode.get(0) + 1);
        HashSet<List<Integer>> result = new HashSet<>();
        double mcdf = this.mcdfCache.mcdf(childNode);
        if (mcdf > this.alpha) {
            this.nodeWeights.put(childNode,1);
            result.add(childNode);
        } else {
            for (int i = 1; i < node.size(); i++) {
                List<Integer> temp = new ArrayList<>(List.copyOf(childNode));
                temp.set(i, temp.get(i) + 1);
                double mcdfTemp = this.mcdfCache.mcdf(temp);
                if (mcdfTemp > this.alpha) {
                    result.add(temp);
                }
            }
        }
        return result;
    }

    public HashSet<List<Integer>> getAllNodesOfLevel(int k) {
        return this.tree.get(k);
    }

    public boolean isAdjusted() {
        return this.doAdjust;
    }

    public double getAlpha() {
        return alpha;
    }

    public double[] getP() {
        return p;
    }

    public int getK() {
        return k;
    }

    public boolean isMinimumProportionsSymmetric() {
        return isMinimumProportionsSymmetric;
    }

    public HashMap<Integer, HashSet<List<Integer>>> getTree() {
        return tree;
    }

    public MCDFCache getMcdfCache() {
        return this.mcdfCache;
    }

    public Integer getWeightOfNode(List<Integer> node){
        if(this.nodeWeights.containsKey(node)){
            return this.nodeWeights.get(node);
        }else{
            throw new IllegalArgumentException("MTree does not contain node.");
        }
    }
}
