package algorithm;

import static org.junit.Assert.*;

import algorithm.MCDFCache;
import algorithm.MTree;
import org.junit.Before;
import org.junit.Test;

import java.util.*;


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
        HashSet<List<Integer>> expected2 = new HashSet<>();
        List<Integer> case2 = new ArrayList<>();
        case2.add(1);
        case2.add(0);
        case2.add(0);
        expected2.add(case2);

        List<Integer> case3 = new ArrayList<>();
        case3.add(1);
        case3.add(0);
        case3.add(1);
        expected2.add(case3);
        List<Integer> case4 = new ArrayList<>();
        case4.add(1);
        case4.add(1);
        case4.add(1);
        expected2.add(case4);
        HashSet<List<Integer>> actual2 = this.emptyMTree.removeMirroredNodes((HashSet<List<Integer>>) expected2.clone());

        assertEquals(expected2, actual2);
        //3. there are some mirror cases
        HashSet<List<Integer>> expected3 = new HashSet<>();
        List<Integer> case5 = new ArrayList<>();
        case5.add(2);
        case5.add(0);
        case5.add(0);
        expected3.add(case5);

        List<Integer> case6 = new ArrayList<>();
        case6.add(2);
        case6.add(0);
        case6.add(1);
        expected3.add(case6);
        List<Integer> case7 = new ArrayList<>();
        case7.add(2);
        case7.add(1);
        case7.add(0);
        expected3.add(case7);
        List<Integer> case8 = new ArrayList<>();
        case8.add(2);
        case8.add(1);
        case8.add(1);
        expected3.add(case8);
        HashSet<List<Integer>> actual3 = this.emptyMTree.removeMirroredNodes((HashSet<List<Integer>>) expected3.clone());

        assertNotEquals(expected3, actual3);
    }

    @Test
    public void testInverseMultinomialCDF() {
        int k = 3;
        double[] p = {1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0};
        double alpha = 0.1;
        HashMap<Integer, HashSet<List<Integer>>> expected = new HashMap<>();
        //Root
        HashSet<List<Integer>> position0 = new HashSet<>();
        List<Integer> node0 = new ArrayList<>();
        node0.add(0);
        node0.add(0);
        node0.add(0);
        position0.add(node0);
        expected.put(0, position0);
        //position 1
        HashSet<List<Integer>> position1 = new HashSet<>();
        List<Integer> node1 = new ArrayList<>();
        node1.add(1);
        node1.add(0);
        node1.add(0);
        position1.add(node1);
        expected.put(1, position1);
        //position 2
        HashSet<List<Integer>> position2 = new HashSet<>();
        List<Integer> node2 = new ArrayList<>();
        node2.add(2);
        node2.add(0);
        node2.add(0);
        position2.add(node2);
        expected.put(2, position2);
        //position 3
        HashSet<List<Integer>> position3 = new HashSet<>();
        List<Integer> node3 = new ArrayList<>();
        node3.add(3);
        node3.add(1);
        node3.add(0);
        position3.add(node3);
        expected.put(3, position3);
        HashMap<Integer, HashSet<List<Integer>>> actual = new MTree(k, p, alpha, false, new MCDFCache(p)).getTree();

        assertEquals(expected, actual);
    }

    @Test
    public void testIsMinimumProportionsSymmetric(){
        int k = 5;
        double alpha = 0.1;

        double[] p1 = {0.5,0.5};
        MTree t1 = new MTree(k,p1,alpha,false,new MCDFCache(p1));
        assertFalse(t1.isMinimumProportionsSymmetric());

        double[] p2 = {0.6,0.4};
        MTree t2 = new MTree(k,p2,alpha,false,new MCDFCache(p2));
        assertFalse(t2.isMinimumProportionsSymmetric());

        double[] p3 = {0.2,0.2,0.2,0.2,0.2};
        MTree t3 = new MTree(k,p3,alpha,false,new MCDFCache(p3));
        assertTrue(t3.isMinimumProportionsSymmetric());

        double[] p4 = {1.0/3.0,1.0/3.0,1.0/3.0};
        MTree t4 = new MTree(k,p4,alpha,false,new MCDFCache(p4));
        assertTrue(t4.isMinimumProportionsSymmetric());
    }
}
