package algorithm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import algorithm.MultinomialFairRanker.FairRankingStrategy;

public class MultinomialFairRankerTest {
    
    /**
     * symmetric mTree looks like this
     * [1, 0, 0]
     * [2, 0, 0]
     * [3, 1, 0]
     * [4, 2, 0], [4, 1, 1]
     * [5, 3, 0], [5, 2, 1], [5, 1, 1]
     * [6, 3, 1], [6, 2, 1]
     * [7, 3, 1], [7, 2, 2]
     * [8, 4, 1], [8, 3, 2], [8, 2, 2]
     * 
     * asymmetric mTree looks like this
     * [1, 0, 0]
     * [2, 0, 0]
     * [3, 1, 0], [3, 0, 1]
     * [4, 2, 0], [4, 1, 1], [4, 0, 1]
     * [5, 2, 1], [5, 1, 1], [5, 0, 2]
     * [6, 2, 1], [6, 1, 1], [6, 1, 2], [6, 0, 3]
     * [7, 2, 1], [7, 1, 2], [7, 0, 3]
     * [8, 2, 2], [8, 1, 2], [8, 1, 3], [8, 0, 4]
     * [9, 2, 2], [9, 1, 3], [9, 1, 4], [9, 0, 5]
     */

    private MultinomialFairRanker rankerSymmetricTree;
    private MultinomialFairRanker rankerAsymmetricTree;
    private List<Candidate> unfairRanking;

    @Before
    public void setup() {
        
        this.unfairRanking = new ArrayList<>();
        unfairRanking.add(0, new Candidate(10.0, 0));
        unfairRanking.add(1, new Candidate(9.0, 0));
        unfairRanking.add(2, new Candidate(8.0, 0));
        unfairRanking.add(3, new Candidate(7.0, 0));

        unfairRanking.add(4, new Candidate(6.0, 1));
        unfairRanking.add(5, new Candidate(5.0, 1));
        unfairRanking.add(6, new Candidate(4.0, 1));

        unfairRanking.add(7, new Candidate(3.0, 2));
        unfairRanking.add(8, new Candidate(2.0, 2));
        
        this.rankerSymmetricTree = new MultinomialFairRanker(9,
                                                             new double[] {1.0/3.0, 1.0/3.0, 1.0/3.0},
                                                             0.1, 
                                                             false, 
                                                             this.unfairRanking);
        
        this.rankerAsymmetricTree = new MultinomialFairRanker(10,
                                                             new double[] {2.0/5.0, 1.0/5.0, 2.0/5.0},
                                                             0.1, 
                                                             false, 
                                                             this.unfairRanking);
        
    }

    @Test
    public void testBuildFairRanking() {
        
        /**
         * SYMMETRIC TREE:
         * Expected Ranking: NP|NP|P1|P2|NP|P1|P2|NP|P1
         */

        List<Candidate> expectedRankingSymmetric = new ArrayList<>();
        expectedRankingSymmetric.add(unfairRanking.get(0));
        expectedRankingSymmetric.add(unfairRanking.get(1));
        expectedRankingSymmetric.add(unfairRanking.get(4));
        expectedRankingSymmetric.add(unfairRanking.get(7));
        expectedRankingSymmetric.add(unfairRanking.get(2));
        expectedRankingSymmetric.add(unfairRanking.get(5));
        expectedRankingSymmetric.add(unfairRanking.get(8));
        expectedRankingSymmetric.add(unfairRanking.get(3));
        expectedRankingSymmetric.add(unfairRanking.get(6));

        List<Candidate> actualRankingSymmetric = this.rankerSymmetricTree.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 9);

        assertArrayEquals(actualRankingSymmetric.toArray(),expectedRankingSymmetric.toArray());
        
        /**
         * ASYMMETRIC TREE:
         * Expected Ranking: NP|NP|P2|NP|P1|NP|P2|NP|P2|P1
         */
        List<Candidate> expectedRankingAsymmetric = new ArrayList<>();
        expectedRankingAsymmetric.add(unfairRanking.get(0));
        expectedRankingAsymmetric.add(unfairRanking.get(1));
        expectedRankingAsymmetric.add(unfairRanking.get(4));
        expectedRankingAsymmetric.add(unfairRanking.get(7));
        expectedRankingAsymmetric.add(unfairRanking.get(2));
        expectedRankingAsymmetric.add(unfairRanking.get(5));
        expectedRankingAsymmetric.add(unfairRanking.get(8));
        expectedRankingAsymmetric.add(unfairRanking.get(3));
        expectedRankingAsymmetric.add(unfairRanking.get(6));

        List<Candidate> actualRankingAsymmetric = this.rankerAsymmetricTree.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 10);

        assertArrayEquals(actualRankingAsymmetric.toArray(),expectedRankingAsymmetric.toArray());
    }
    
    @Test
    public void testBuildFairRanking_NotEnoughProtected() {
        
        fail("Not yet implemented");
    }
    
    @Test
    public void testBuildFairRanking_MirroredNodes() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testGetTheNode() {
        List<Integer> expected = Arrays.stream(new int[] {4, 1, 1}).boxed().collect(Collectors.toList());
        List<Integer> actual = this.rankerSymmetricTree.getTheNode(FairRankingStrategy.MOST_LIKELY, 4);
        assertEquals(expected, actual);
    }

}
