package algorithm;

import umontreal.ssj.probdistmulti.MultinomialDist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

//FIXME: Implement Serializable for MCDFCache https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/Serializable.html
public class MCDFCache implements Serializable {

    private double[] p;
    private HashMap<List<Integer>,Double> mcdfCache;
    private boolean minimumProportionsAreEqual;

    public MCDFCache(double[] p){
        this.p = p;
        this.mcdfCache = new HashMap<>();
        this.minimumProportionsAreEqual = MTree.checkIfMinimumProportionsAreEqual(this.p);

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
}