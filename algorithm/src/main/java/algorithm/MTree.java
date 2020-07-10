package algorithm;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MTree implements Serializable {

    public enum FairRankingStrategy {
        /**
         * enum to choose from different ranking strategies -- MOST_LIKELY = child node
         * with highest mcdf, RANDOM = pick random child node
         */
        MOST_LIKELY, RANDOM;
    }

    private static final long serialVersionUID = -5797820121404671859L;
    public static double EPS = 0.001;
    public static int K_STEP_LOWER_BOUND = 25;
    public static int REGRESSION_ITERATIONS = 5;

    private double alpha;
    private double unadjustedAlpha;
    private double[] p;
    private int k;
    /**
     * Stores the minimum number of protected candidates needed at each position for
     * all protected groups. The inner HashSet represents all valid nodes of a
     * particular level in the mTree without duplicates and without mirrors.
     */
    private HashMap<Integer, HashSet<List<Integer>>> tree;
    private boolean doAdjust;
    private transient MCDFCache mcdfCache;
    private boolean isMinimumProportionsSymmetric;
    private FailProbabilityEstimator failProbabilityEstimator;

    public MTree(int k, double[] p, double alpha, boolean doAdjust) {
        MTree loadedMTree = Serializer.loadMTree(k, p, alpha, doAdjust);
        if (loadedMTree != null) {
            System.out.println("loaded MTree for k="+loadedMTree.getK());
            this.loadMTreeFromSerializedObject(loadedMTree);
        } else {
            this.k = k;
            this.p = p;
            this.alpha = alpha;
            this.unadjustedAlpha = alpha;
            this.doAdjust = doAdjust;
            this.mcdfCache = Serializer.loadMCDFCache(p);
            if (this.mcdfCache == null) {
                this.mcdfCache = new MCDFCache(p);
            }else{
                System.out.println('\n'+"======MCDF CACHE FROM STORAGE YAY!!!========"+'\n');
            }

            // check if we have symmetric proportions p[] to allow later optimizations
            this.isMinimumProportionsSymmetric = MTree.checkIfMinimumProportionsAreEqual(this.p);

            // check if Alpha Adjustment shall be used
            if (doAdjust) {
                System.out.print(".");
                this.tree = this.regressionAdjustment(this.k / 2, REGRESSION_ITERATIONS);
                this.store(); //Store Mtree and MCDF Cache to file for later use
            } else {
                System.out.print(".");
                this.tree = this.buildMTree();
                this.store(); //Store Mtree and MCDF Cache to file for later use
            }
        }
    }

    public MTree(int k, double[] p, double alpha, boolean doAdjust, boolean useMemory, boolean useRegression) {
        if (useMemory) {
            loadMTreeFromSerializedObject(new MTree(k, p, alpha, doAdjust));
        } else {
            this.k = k;
            this.p = p;
            this.alpha = alpha;
            this.unadjustedAlpha = alpha;
            this.doAdjust = doAdjust;
            this.mcdfCache = new MCDFCache(p);
            // check if we have symmetric proportions p[] to allow later optimizations
            this.isMinimumProportionsSymmetric = MTree.checkIfMinimumProportionsAreEqual(this.p);
            // check if Alpha Adjustment shall be used
            if (doAdjust) {
                if (useRegression) {
                    this.tree = this.regressionAdjustment(this.k / 2, REGRESSION_ITERATIONS);
                } else {
                    this.tree = this.buildAdjustedMTree();
                }
            } else {
                this.tree = this.buildMTree();
            }
        }
    }

    private void loadMTreeFromSerializedObject(MTree loadedMTree) {
        this.k = loadedMTree.getK();
        this.p = loadedMTree.getP();
        this.alpha = loadedMTree.getAlpha();
        this.unadjustedAlpha = loadedMTree.getUnadjustedAlpha();
        this.doAdjust = loadedMTree.isAdjusted();
        this.isMinimumProportionsSymmetric = loadedMTree.isMinimumProportionsSymmetric();
        this.failProbabilityEstimator = loadedMTree.failProbabilityEstimator;
        this.tree = loadedMTree.getTree();
        this.mcdfCache = Serializer.loadMCDFCache(p);
        if (this.mcdfCache == null) {
            this.mcdfCache = new MCDFCache(p);
        }
    }

    public HashMap<Integer, HashSet<List<Integer>>> regressionAdjustment(int maxPreAdjustK, int iterations) {

        int kStep = this.k / 4;
        int stepsize = Math.max(maxPreAdjustK / iterations, 1);
        if (kStep <= K_STEP_LOWER_BOUND || maxPreAdjustK <= iterations || kStep + stepsize > maxPreAdjustK) {
            return this.buildAdjustedMTree();
        } else {
            int kTarget = this.k;
            final WeightedObservedPoints obs = new WeightedObservedPoints();
            double originalAlpha = alpha;
            MTree tree = new MTree(kStep, p, alpha, true);
            System.out.print(".");
            kStep += stepsize;
            alpha = tree.getAlpha();
            obs.add(kStep, alpha);
            for (int i = 1; i < iterations; i++) {
                System.out.print(".");
                tree = new MTree(kStep, p, alpha, true);
                alpha = tree.getAlpha();
                obs.add(kStep, alpha);
                if (kStep + stepsize <= maxPreAdjustK && kStep + stepsize <= kTarget) {
                    kStep += stepsize;
                } else {
                    break;
                }
            }
            if (kStep == kTarget) {
                this.alpha = tree.getAlpha();
                return tree.getTree();
            }
            //Add data points in case of too few iterations in the beginning
            //This is legit because the curve fitting will else be negative for k > maxPreAdjustK
            obs.add(10000, 0.0);
            obs.add(kTarget * 1000, 0.0);

            final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
            final double[] coeff = fitter.fit(obs.toList());
            double alphaPredict = Math.max(0.0, coeff[0] + coeff[1] * kTarget + coeff[2] * (kTarget * kTarget));
            tree = new MTree(k, p, alphaPredict, false);
            double failProbPredict = tree.getFailprob();
            if (failProbPredict > originalAlpha) {
                return postRegressionAdjustment(alphaPredict, originalAlpha, false);
            }
            if (failProbPredict < originalAlpha) {
                return postRegressionAdjustment(alphaPredict, originalAlpha, true);
            } else {
                return tree.getTree();
            }
        }
    }

    private HashMap<Integer, HashSet<List<Integer>>> postRegressionAdjustment(double alphaPredict, double originalAlpha, boolean predictionIsMin) {
        double aMin;
        double aMax;
        double aMid;
        System.out.println("Started post-regression adjustment:");
        if (predictionIsMin) {
            aMin = alphaPredict;
            aMax = originalAlpha;
        } else {
            aMin = 0.0;
            aMax = alphaPredict;
        }
        aMid = (aMin + aMax) / 2.0;
        MTree mid = new MTree(k, p, aMid, false);
        System.out.print(".");
        if (mid.getFailprob() == 0 || Math.abs(mid.getFailprob() - originalAlpha) <= EPS) {
            return mid.tree;
        }
        MTree max = new MTree(this.k, this.p, aMax, false);
        System.out.print(".");
        MTree min = new MTree(k, p, aMin, false);
        System.out.print(".");

        while (true) {
            System.out.print(".");
            if (mid.getFailprob() == originalAlpha) {
                this.unadjustedAlpha = originalAlpha;
                this.alpha = mid.getAlpha();
                return mid.tree;
            }
            if (mid.getFailprob() < originalAlpha) {
                aMin = aMid;
                min = new MTree(k, p, aMin, false);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid, false);
            } else if (mid.getFailprob() > this.alpha) {
                aMax = aMid;
                max = new MTree(k, p, aMax, false);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid, false);
            }

            double midDiff = Math.abs(mid.getFailprob() - originalAlpha);
            double maxDiff = Math.abs(max.getFailprob() - originalAlpha);
            double minDiff = Math.abs(min.getFailprob() - originalAlpha);
            if (midDiff <= EPS && (midDiff <= maxDiff && midDiff <= minDiff)) {
                this.unadjustedAlpha = originalAlpha;
                this.alpha = mid.getAlpha();
                return mid.tree;
            }
            if (minDiff <= EPS && (minDiff <= maxDiff && minDiff <= midDiff)) {
                this.unadjustedAlpha = originalAlpha;
                this.alpha = min.getAlpha();
                return min.tree;
            }
            if (maxDiff <= EPS && maxDiff <= minDiff && maxDiff <= midDiff) {
                this.unadjustedAlpha = originalAlpha;
                this.alpha = max.getAlpha();
                return max.tree;
            }
        }
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildAdjustedMTree() {
        System.out.println("building adjusted mtree with binary search");
        double aMin = 0;
        double aMax = this.alpha;
        double aMid = (aMin + aMax) / 2.0;

        MTree max = new MTree(this.k, this.p, aMax, false);
        if (max.getFailprob() == 0 || Math.abs(max.getFailprob() - this.alpha) <= EPS) {
            return max.tree;
        }
        MTree min = new MTree(k, p, aMin, false);
        MTree mid = new MTree(k, p, aMid, false);

        while (true) {
            if (mid.getFailprob() == this.alpha) {
                this.unadjustedAlpha = this.alpha;
                this.alpha = mid.getAlpha();
                return mid.tree;
            }
            if (mid.getFailprob() < this.alpha) {
                aMin = aMid;
                min = new MTree(k, p, aMin, false);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid, false);
            } else if (mid.getFailprob() > this.alpha) {
                aMax = aMid;
                max = new MTree(k, p, aMax, false);
                aMid = (aMin + aMax) / 2.0;
                mid = new MTree(k, p, aMid, false);
            }

            double midDiff = Math.abs(mid.getFailprob() - this.alpha);
            double maxDiff = Math.abs(max.getFailprob() - this.alpha);
            double minDiff = Math.abs(min.getFailprob() - this.alpha);
            if (midDiff <= EPS && (midDiff <= maxDiff && midDiff <= minDiff)) {
                this.unadjustedAlpha = this.alpha;
                this.alpha = mid.getAlpha();
                return mid.tree;
            }
            if (minDiff <= EPS && (minDiff <= maxDiff && minDiff <= midDiff)) {
                this.unadjustedAlpha = this.alpha;
                this.alpha = min.getAlpha();
                return min.tree;
            }
            if (maxDiff <= EPS && maxDiff <= minDiff && maxDiff <= midDiff) {
                this.unadjustedAlpha = this.alpha;
                this.alpha = max.getAlpha();
                return max.tree;
            }
        }
    }

    public double getAlpha() {
        return this.alpha;
    }

    public double getFailprob() {
        if (this.failProbabilityEstimator == null) {
            this.failProbabilityEstimator = new FailProbabilityEstimator(this);
            return this.failProbabilityEstimator.getFailProbability();
        }
        return failProbabilityEstimator.getFailProbability();
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildMTree() {
        HashMap<Integer, HashSet<List<Integer>>> tree = new HashMap<>();
        int position = 0;

        // Create root node and fill it with zero entries
        // we need the root node for initialisation it represents ranking position 0
        // which is no real position
        List<Integer> root = new ArrayList<>(Arrays.asList(new Integer[this.p.length]));
        Collections.fill(root, 0);
        HashSet<List<Integer>> positionZero = new HashSet<>();
        positionZero.add(root);
        tree.put(position, positionZero);
        System.out.println('\n'+"computing MTree for p="+Arrays.toString(this.p) + ", alpha="+this.alpha +", k="+this.k+" level of Tree: ");
        System.out.print("Currently at position: ");
        while (position < this.k) {
            System.out.print(position+" ");
            HashSet<List<Integer>> currentLevel = tree.get(position);
            HashSet<List<Integer>> currentChildCandidates = new HashSet<>();
            for (List<Integer> node : currentLevel) {
                currentChildCandidates.addAll(inverseMultinomialCDF(node));
            }
            // if minimum proportions in p[] are equal, we can delete many "mirrored" nodes
            // and thus be more efficient
            if (this.isMinimumProportionsSymmetric) {
                currentChildCandidates = removeMirroredNodes(currentChildCandidates);
            }
            position++;
            tree.put(position, currentChildCandidates);
            Serializer.storeMCDFCache(this.mcdfCache);
        }
        System.out.println('\n');
        return tree;
    }

    public HashSet<List<Integer>> removeMirroredNodes(HashSet<List<Integer>> currentChildCandidates) {
        // [1,1,1] ----> darf sich nicht selbst löschen
        /// 1. Nehme einen Knoten aus dem set
        // 2. Copy and Mirror den Knoten
        // 3. Gucke ob Mirror = Knoten ? Ja --> nächster Knoten Nein? ---> 4.
        // 4. Gibt es den im Set?

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

    public static List<Integer> mirror(List<Integer> node) {
        List<Integer> node_sublist = node.subList(1, node.size());
        List<Integer> mirror = new ArrayList<>();
        for (Integer m_i : node_sublist) {
            mirror.add(0, m_i);
        }
        mirror.add(0, node.get(0));
        return mirror;
    }

    public static boolean checkIfMinimumProportionsAreEqual(double[] p) {
        if (p.length <= 2) {
            // we only have one protected group
            return false;
        } else {
            for (int i = 1; i < p.length; i++) {
                for (int j = 1; j < p.length; j++) {
                    if (p[i] != p[j]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private HashSet<List<Integer>> inverseMultinomialCDF(List<Integer> node) {
        List<Integer> childNode = new ArrayList<>(List.copyOf(node));
        childNode.set(0, childNode.get(0) + 1);
        HashSet<List<Integer>> result = new HashSet<>();
        double mcdf = this.mcdfCache.mcdf(childNode);
        if (mcdf > this.alpha) {
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

    protected HashSet<List<Integer>> getActualChildren(List<Integer> thisNode) {
        /**
         * we have to retrieve the parent-child relationship for each tree layer in
         * order to find a continuous path through the tree
         *
         * @returns all nodes that are actual children of @thisNode
         */
        HashSet<List<Integer>> actualChildren = new HashSet<>();
        int nextPosition = thisNode.get(0) + 1;
        for (List<Integer> mNode : getAllNodesOfTreeLevel(nextPosition)) {
            ArrayList<Integer> nodeDistance = new ArrayList<Integer>();
            for (int i = 0; i < thisNode.size(); i++) {
                nodeDistance.add(mNode.get(i) - thisNode.get(i));
            }
            if (nodeDistance.stream().anyMatch(i -> i < 0)) {
                // a child cannot have a lower value than thisNode at any Integer in the node
                continue;
            } else {
                switch (nodeDistance.stream().reduce(0, Integer::sum)) {
                    case 1:
                        // the child and thisNode have the same signature, in this case it is the only
                        // child
                        // any child candidates that may have been found before are invalid
                        actualChildren.removeAll(actualChildren);
                        actualChildren.add(mNode);
                        return actualChildren;
                    case 2:
                        // the child and thisNode have a distance of 1, which makes it a possible child,
                        // if no child with the same node signature is found later
                        actualChildren.add(mNode);
                    default:
                        // a node distance larger than 2 indicates that this node is not a possible
                        // child
                        break;
                }
            }
        }

        return actualChildren;
    }

    protected List<Integer> getCorrectChildNode(FairRankingStrategy strategy, List<Integer> parent) {
        /**
         * from all possible mNodes at this layer, returns the node that fits the
         * defined strategy
         *
         * if the minimum proportions are symmetric, we have a symmetric tree and hence
         * all nodes may have mirror nodes that have the same mcdf value. In this case
         * they should be equally likely to be picked.
         *
         * @param strategy: enum to choose from different ranking strategies --
         *        MOST_LIKELY = child node with highest mcdf MOST_UNLIKELY = child with
         *        lowest mcdf (still valid though) RANDOM = pick random child node
         *
         */
        HashSet<List<Integer>> children = getActualChildren(parent);

        // get any node of this layer in the mTree to initialize
        List<Integer> result = new ArrayList<>();

        switch (strategy) {
            case MOST_LIKELY:
                Double highestMCDF = 0.0;
                // find the likeliest node from all possible children (which are not all nodes
                // at this layer)
                for (List<Integer> mNode : children) {
                    if (mcdfCache.mcdf(mNode) > highestMCDF) {
                        highestMCDF = mcdfCache.mcdf(mNode);
                        // for a symmetric tree check if mirror node is also in the set of children and has the same mcdf
                        if (isMinimumProportionsSymmetric && children.contains(mirror(mNode))) {
                            // pick one of them at random
                            result = 0.5 >= Math.random() ? mNode : mirror(mNode);
                        } else {
                            result = mNode;
                        }
                    }
                }
                break;
            case RANDOM:
                // from all elements in set, pick one at random from uniform distributed
                // randomness
                int randomIndex = new Random().nextInt(children.size());
                Iterator<List<Integer>> iter = children.iterator();
                for (int i = 0; i < randomIndex; i++) {
                    iter.next();
                }
                result = iter.next();
                break;
            default:
                throw new IllegalArgumentException("strategy must be either MOST_LIKELY or RANDOM");
        }

        return result;
    }

    public HashSet<List<Integer>> getAllNodesOfTreeLevel(int level) {
        if (this.isMinimumProportionsSymmetric) {
            // we have to recreate all mirrored nodes
            HashSet<List<Integer>> result = new HashSet<>(tree.get(level));
            for (List<Integer> node : tree.get(level)) {
                List<Integer> mirrorNode = mirror(node);
                result.add(mirrorNode);
            }
            return result;
        } else {
            return tree.get(level);
        }
    }

    public List<Integer> getRoot() {
        return this.tree.get(0).iterator().next();
    }

    public boolean isAdjusted() {
        return this.doAdjust;
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

    public String toString() {
        String result = new String();
        for (int pos = 1; pos < this.k; pos++) {
            HashSet<List<Integer>> nodes = this.tree.get(pos);
            String line = nodes.stream().map(Object::toString).collect(Collectors.joining(", "));
            result = result + line + "\n";
        }
        return result;
    }

    public double getUnadjustedAlpha() {
        return this.unadjustedAlpha;
    }

    public void store() {
        Serializer.storeMTree(this);
        Serializer.storeMCDFCache(this.mcdfCache);
    }

    public static void main(String[] args){
        double alpha = 0.1;
        double[] p1 = {0.18,0.11,0.1,0.39,0.06,0.16};
        double[] p3 = {0.26,0.55,0.08,0.11};
        double[] p4 = {0.25,0.25,0.25,0.25};
        double[] p5 = {0.46,0.17,0.04,0.11,0.18,0.04};
        ArrayList<double[]> plist = new ArrayList<>();
        plist.add(p1);
        plist.add(p3);
        plist.add(p4);
        plist.add(p5);
        for(int k = 20; k<=500; k+=5) {
            for (double[] p : plist) {
                MTree tree = new MTree(k, p, alpha, false);
            }
        }
    }
}
