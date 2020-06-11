package algorithm;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import algorithm.MultinomialFairRanker.FairRankingStrategy;

public class MTree implements Serializable {

    public static double EPS = 0.001;

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
    private HashMap<List<Integer>, Integer> nodeWeights; // FIXME: write comment about how this data structure looks
    // like
    private boolean doAdjust;
    private MCDFCache mcdfCache;
    private boolean isMinimumProportionsSymmetric;
    private FailProbabilityEstimator failProbabilityEstimator;

    public MTree(int k, double[] p, double alpha, boolean doAdjust) {
        MTree loadedMTree = Serializer.loadMTree(k, p, alpha, doAdjust);
        if (loadedMTree != null) {
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
            }
            this.nodeWeights = new HashMap<>();

            // check if we have symmetric proportions p[] to allow later optimizations
            this.isMinimumProportionsSymmetric = MTree.checkIfMinimumProportionsAreEqual(this.p);

            // check if Alpha Adjustment shall be used
            if (doAdjust) {
                this.tree = this.buildAdjustedMTree();
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
        this.nodeWeights = loadedMTree.nodeWeights;
        this.tree = loadedMTree.getTree();
        this.mcdfCache = loadedMTree.getMcdfCache();
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildAdjustedMTree() {
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

    private double getFailprob() {
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

        while (position < this.k) {
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
        }
        //FIXME: This will produce a LOT of files if we adjust
        //Serializer.storeMTree(this);
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
            this.nodeWeights.put(childNode, 1);
            result.add(childNode);
        } else {
            for (int i = 1; i < node.size(); i++) {
                List<Integer> temp = new ArrayList<>(List.copyOf(childNode));
                temp.set(i, temp.get(i) + 1);
                double mcdfTemp = this.mcdfCache.mcdf(temp);
                if (mcdfTemp > this.alpha) {
                    this.nodeWeights.put(temp, 1);
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
        int nextPosition = thisNode.get(0);
        for (List<Integer> mNode : this.tree.get(nextPosition)) {
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
                        // a node distance larger than 2 indicates that this node is not a possible child
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
        List<Integer> result = children.iterator().next();

        switch (strategy) {
            case MOST_LIKELY:
                Double highestMCDF = 0.0;
                // find the likeliest node from all possible children (which are not all nodes at this layer)
                for (List<Integer> mNode : children) {
                    if (mcdfCache.mcdf(mNode) > highestMCDF) {
                        highestMCDF = mcdfCache.mcdf(mNode);
                        result = mNode;
                    }
                }
                break;
            case MOST_UNLIKELY:
                Double lowestMCDF = 1.0;
                // find the unlikeliest node
                for (List<Integer> mNode : children) {
                    if (mcdfCache.mcdf(mNode) < lowestMCDF) {
                        lowestMCDF = mcdfCache.mcdf(mNode);
                        result = mNode;
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
                throw new IllegalArgumentException("strategy must be either MOST_LIKELY, MOST_UNLIKELY or RANDOM");
        }
        return result;
    }

    public List<Integer> getRoot() {
        return this.tree.get(0).iterator().next();
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

    public Integer getWeightOfNode(List<Integer> node) {
        if (this.nodeWeights.containsKey(node)) {
            return this.nodeWeights.get(node);
        } else {
            throw new IllegalArgumentException("MTree does not contain node.");
        }
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
}
