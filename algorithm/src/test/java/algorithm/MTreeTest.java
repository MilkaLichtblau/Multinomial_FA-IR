package algorithm;

import static org.junit.Assert.*;

import algorithm.MCDFCache;
import algorithm.MTree;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MTreeTest {

    private MTree emptyMTree;

    @Before
    public void setup() {
        int k = 0;
        double[] p = {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0};
        double alpha = 0.1;
        this.emptyMTree = new MTree(k, p, alpha, false, new MCDFCache(p));
    }

    @Test
    public void testRemoveMirroredNodes() {
        //1. [1,1,1] case
        HashSet<List<Integer>> expected = new HashSet<>();
        List<Integer> case1 = new ArrayList<>();

        case1.add(1);
        case1.add(1);
        case1.add(1);
        expected.add(case1);

        HashSet<List<Integer>> actual = this.emptyMTree.removeMirroredNodes(expected);

        assertEquals(expected, actual);
        //2. there is no mirror case
        //3. there is are some mirrors case
        //4. longer and shorter(binomial) nodes

    }
}
