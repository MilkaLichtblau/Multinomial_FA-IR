package algorithm;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class FailProbabilityEstimatorTest {

    @Test
    public void testEnoughProtectedSymmetricTree(){
        double[] p = {1.0/3.0,1.0/3.0,1.0/3.0};
        int k = 15;
        double alpha = 0.1;
        FailProbabilityEstimator estimator = new FailProbabilityEstimator(new MTree(k,p,alpha,false));
        List<Integer> node = new ArrayList<>();
        node.add(3);
        node.add(0);
        node.add(1);
        int[] seenSoFar1 = {3,1,0};
        int[] seenSoFar2 = {3,0,1};
        assertTrue(estimator.enoughProtected(node,seenSoFar1) && estimator.enoughProtected(node,seenSoFar2));
    }

    @Test
    public void testEnoughProtectedNotSymmetric(){
        double[] p = {0.5,0.2,0.3};
        int k = 15;
        double alpha = 0.1;
        FailProbabilityEstimator estimator = new FailProbabilityEstimator(new MTree(k,p,alpha,false));
        List<Integer> node = new ArrayList<>();
        node.add(3);
        node.add(0);
        node.add(1);
        int[] seenSoFar1 = {3,1,0};
        int[] seenSoFar2 = {3,0,1};
        assertTrue(!estimator.enoughProtected(node,seenSoFar1) && estimator.enoughProtected(node,seenSoFar2));
    }
}
