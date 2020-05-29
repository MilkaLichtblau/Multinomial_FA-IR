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
            //this.tree = this.buildAdjustedMTree();
        } else {
            this.tree = this.buildMTree();
        }
    }

    private HashMap<Integer, HashSet<List<Integer>>> buildMTree() {
        HashMap<Integer, HashSet<List<Integer>>> tree = new HashMap<>();
        int position = 0;
        //if minimum proportions in p[] are equal, we can delete many "mirrored" nodes and thus be more efficient
        boolean isSymmetricPreferences = isMinimumProportionsSymmetric();
        //Create root node and fill it with zero entries
        //we need the root node for initialisation it represents ranking position 0 which is no real position
        //FIXME: Maybe delete root node after creating the mtree
        List<Integer> root = new ArrayList<>(Collections.nCopies(0, this.p.length));
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
                if(currentChildCandidates.contains(mirror)){
                    currentChildCandidates.remove(mirror);
                    break;
                }
            }
        }
        return currentChildCandidates;
    }

    private List<Integer> mirror(List<Integer> node) {
        List<Integer> mirror = new ArrayList<>();
        for (Integer m_i : node) {
            mirror.add(0, m_i);
        }
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
    private boolean isMinimumProportionsSymmetric() {
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

}
