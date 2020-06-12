package algorithm;

import umontreal.ssj.probdistmulti.MultinomialDist;

import java.io.Serializable;
import java.util.*;

public class MCDFCache implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7599506547007946239L;
    private double[] p;
    private HashMap<List<Integer>,Double> mcdfCache;
    private boolean minimumProportionsAreEqual;

    public MCDFCache(double[] p){
        this.p = p;
        this.mcdfCache = new HashMap<>();
        this.minimumProportionsAreEqual = MTree.checkIfMinimumProportionsAreEqual(this.p);

    }
    public int getMaxK(){
        List<Integer> maximum = mcdfCache.keySet().stream().max(Comparator.comparing(l -> l.get(0))).get();
        return maximum.get(0);
    }

    public int getSize(){
        return mcdfCache.size();
    }

    public double[] getP(){
        return this.p;
    }

    public double mcdf(List<Integer> signature){
        if(mcdfCache.get(signature)==null){
            int trials = signature.get(0);
            int[] signatureAsArray = new int[p.length];
            for(int i = 0; i<p.length; i++){
                signatureAsArray[i] = signature.get(i);
            }
            double cdf = MultinomialDist.cdf(trials, p, signatureAsArray);
            mcdfCache.put(signature, cdf);
            //Store mirror without mcdf computation if min proportions are equal
            if(minimumProportionsAreEqual){
                mcdfCache.put(MTree.mirror(signature), cdf);
            }
            return cdf;
        }
        return mcdfCache.get(signature);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(List<Integer> key : mcdfCache.keySet()){
            stringBuilder.append(key);
            stringBuilder.append(" : ");
            stringBuilder.append(mcdfCache.get(key));
            stringBuilder.append('\n');
        }
        stringBuilder.append("p=");
        stringBuilder.append(Arrays.toString(p));
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MCDFCache)) return false;
        MCDFCache cache = (MCDFCache) o;
        return minimumProportionsAreEqual == cache.minimumProportionsAreEqual &&
                Arrays.equals(getP(), cache.getP()) &&
                mcdfCache.equals(cache.mcdfCache);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mcdfCache, minimumProportionsAreEqual);
        result = 31 * result + Arrays.hashCode(getP());
        return result;
    }
}