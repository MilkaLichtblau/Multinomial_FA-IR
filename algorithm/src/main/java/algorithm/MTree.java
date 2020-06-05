package algorithm;

import java.util.*;

public class MTree {

    private double alpha;
    public double unadjustedAlpha;
    private double[] p;
    private int k;
    private HashMap<Integer, HashSet<List<Integer>>> tree;
    private boolean doAdjust;
    private MCDFCache mcdfCache;


    public MTree(int k, double[] p, double alpha, boolean doAdjust, MCDFCache mcdfCache) {
        this.k = k;
        this.p = p;
        this.alpha = alpha;
        this.unadjustedAlpha = alpha;
        this.doAdjust = doAdjust;
        this.mcdfCache = mcdfCache;
        if (doAdjust) {
            this.tree = this.buildAdjustedMTree();
        } else {
            this.tree = this.buildMTree();
        }
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildAdjustedMTree() {
        //TODO: Refactor this
//            double aMin = 0;
//            double aMax = this.alpha;
//            double aMid = (aMin + aMax) / 2.0;
//
//            MTree max = new MTree(this.k, this.p, aMax,false, this.mcdfCache);
//            if (max.getFailprob() == 0) {
//                return max.tree;
//            }
//            MultinomialMTableFailProbPair min = new MultinomialMTableFailProbPair(k, p, aMin, mcdfCache);
//            MultinomialMTableFailProbPair mid = new MultinomialMTableFailProbPair(k, p, aMid, mcdfCache);
//
//            if (Math.abs(max.getFailprob() - alphaOld) <= tolerance) {
//                return max;
//            }
//            while (true) {
//                boolean trigger = false;
//                char side = '0';
//                if (mid.getFailprob() < alphaOld) {
//                    aMin = aMid;
//                    trigger = true;
//                    side = 'l';
//                } else if (mid.getFailprob() > alphaOld) {
//                    aMax = aMid;
//                    trigger = true;
//                    side = 'r';
//                }
//                if (trigger && side == 'l') {
//                    min = new MultinomialMTableFailProbPair(k, p, aMin, mcdfCache);
//                    aMid = (aMin + aMax) / 2.0;
//                    mid = new MultinomialMTableFailProbPair(k, p, aMid, mcdfCache);
//                } else if (trigger && side == 'r') {
//                    max = new MultinomialMTableFailProbPair(k, p, aMax, mcdfCache);
//                    aMid = (aMin + aMax) / 2.0;
//                    mid = new MultinomialMTableFailProbPair(k, p, aMid, mcdfCache);
//                }
//
//                double midDiff = Math.abs(mid.getFailprob() - alphaOld);
//                double maxDiff = Math.abs(max.getFailprob() - alphaOld);
//                double minDiff = Math.abs(min.getFailprob() - alphaOld);
//                if (midDiff <= tolerance && midDiff <= maxDiff && midDiff <= minDiff) {
////                System.out.println("MID:Failprob: " + mid.getFailprob() + " ; k: " + k);
//                    return mid;
//                }
//                if (minDiff <= tolerance && minDiff <= maxDiff && minDiff <= midDiff) {
////                System.out.println("MIN:Failprob: " + min.getFailprob() + " ; k: " + k);
//                    return min;
//                }
//                if (maxDiff <= tolerance && maxDiff <= minDiff && maxDiff <= midDiff) {
////                System.out.println("MAX:Failprob: " + max.getFailprob() + " ; k: " + k);
//                    return max;
//                }
////            System.out.println("midDiff: " + midDiff);
//            }
        return null;
    }

    private double getFailprob() {
        //TODO: Implement with experimental failprob calculation
        return 0;
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildMTree() {
        HashMap<Integer, HashSet<List<Integer>>> tree = new HashMap<>();
        int position = 0;

        //if minimum proportions in p[] are equal, we can delete many "mirrored" nodes and thus be more efficient
        boolean isSymmetricPreferences = isMinimumProportionsSymmetric();

        //Create root node and fill it with zero entries
        //we need the root node for initialisation it represents ranking position 0 which is no real position
        //FIXME: Maybe delete root node after creating the mtree
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
            if (isSymmetricPreferences) {
                currentChildCandidates = removeMirroredNodes(currentChildCandidates);
            }
            position++;
            tree.put(position, currentChildCandidates);
        }
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

    /**
     * Method that checks if minimum proportions in p[] are equal
     *
     * @return true if they are equal, otherwise false
     */
    public boolean isMinimumProportionsSymmetric() {
        if (p.length <= 2) {
            return false;
        }
        for (int i = 1; i < p.length; i++) {
            for (int j = 1; j < p.length; j++) {
                if (p[i] != p[j]) {
                    return false;
                }
            }
        }
        return true;
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

    public HashMap<Integer, HashSet<List<Integer>>> getTree() {
        return tree;
    }
}
