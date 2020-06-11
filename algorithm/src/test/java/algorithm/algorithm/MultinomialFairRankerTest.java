package algorithm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import algorithm.MultinomialFairRanker.FairRankingStrategy;

public class MultinomialFairRankerTest {

    /**
     * symmetric mTree looks like this [1, 0, 0] [2, 0, 0] [3, 1, 0] [4, 2, 0], [4,
     * 1, 1] [5, 3, 0], [5, 2, 1], [5, 1, 1] [6, 3, 1], [6, 2, 1] [7, 3, 1], [7, 2,
     * 2] [8, 4, 1], [8, 3, 2], [8, 2, 2]
     * 
     * asymmetric mTree looks like this [1, 0, 0] [2, 0, 0] [3, 1, 0], [3, 0, 1] [4,
     * 2, 0], [4, 1, 1], [4, 0, 1] [5, 2, 1], [5, 1, 1], [5, 0, 2] [6, 2, 1], [6, 1,
     * 1], [6, 1, 2], [6, 0, 3] [7, 2, 1], [7, 1, 2], [7, 0, 3] [8, 2, 2], [8, 1,
     * 2], [8, 1, 3], [8, 0, 4] [9, 2, 2], [9, 1, 3], [9, 1, 4], [9, 0, 5]
     */

    private List<Candidate> unfairRanking;

    // group 0
    private Candidate candidate_11_0 = new Candidate(11.0, 0);
    private Candidate candidate_10_0 = new Candidate(10.0, 0);
    private Candidate candidate_09_0 = new Candidate(9.0, 0);
    private Candidate candidate_08_0 = new Candidate(8.0, 0);
    private Candidate candidate_07_0 = new Candidate(7.0, 0);

    // group 1
    private Candidate candidate_06_1 = new Candidate(6.0, 1);
    private Candidate candidate_05_1 = new Candidate(5.0, 1);
    private Candidate candidate_04_1 = new Candidate(4.0, 1);

    // group 2
    private Candidate candidate_03_2 = new Candidate(3.0, 2);
    private Candidate candidate_02_2 = new Candidate(2.0, 2);
    private Candidate candidate_01_2 = new Candidate(1.0, 2);

    @Before
    public void setup() {

        this.unfairRanking = new ArrayList<>();
        unfairRanking.add(candidate_11_0);
        unfairRanking.add(candidate_10_0);
        unfairRanking.add(candidate_09_0);
        unfairRanking.add(candidate_08_0);
        unfairRanking.add(candidate_07_0);

        unfairRanking.add(candidate_06_1);
        unfairRanking.add(candidate_05_1);
        unfairRanking.add(candidate_04_1);

        unfairRanking.add(candidate_03_2);
        unfairRanking.add(candidate_02_2);
        unfairRanking.add(candidate_01_2);

    }

    @Test
    public void testBuildFairRanking_symmetricTreeMostLikely() {

        /**
         * FIXME: check for mirrored nodes SYMMETRIC TREE: Expected Ranking:
         * NP|NP|P1|P2|NP|P1|P2|NP|P1
         */

        MultinomialFairRanker ranker = new MultinomialFairRanker(9, new double[] { 1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0 },
                0.1, false, this.unfairRanking);

        List<Candidate> expectedRankingSymmetric = new ArrayList<>();
        expectedRankingSymmetric.add(candidate_11_0);
        expectedRankingSymmetric.add(candidate_10_0);
        expectedRankingSymmetric.add(candidate_06_1);
        expectedRankingSymmetric.add(candidate_03_2);
        expectedRankingSymmetric.add(candidate_09_0);
        expectedRankingSymmetric.add(candidate_05_1);
        expectedRankingSymmetric.add(candidate_02_2);
        expectedRankingSymmetric.add(candidate_08_0);
        expectedRankingSymmetric.add(candidate_04_1);

        List<Candidate> actualRankingSymmetric = ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 9);

        assertArrayEquals(actualRankingSymmetric.toArray(), expectedRankingSymmetric.toArray());
    }

    @Test
    public void testBuildFairRanking_asymmetricTreeMostLikely() {

        /**
         * ASYMMETRIC TREE most likely path: Expected Ranking:
         * NP|NP|P2|NP|P1|NP|P2|NP|P2|P1
         */

        MultinomialFairRanker ranker = new MultinomialFairRanker(10, new double[] { 2.0 / 5.0, 1.0 / 5.0, 2.0 / 5.0 },
                0.1, false, this.unfairRanking);

        List<Candidate> expectedRankingAsymmetric = new ArrayList<>();
        expectedRankingAsymmetric.add(candidate_11_0);
        expectedRankingAsymmetric.add(candidate_10_0);
        expectedRankingAsymmetric.add(candidate_03_2);
        expectedRankingAsymmetric.add(candidate_09_0);
        expectedRankingAsymmetric.add(candidate_06_1);
        expectedRankingAsymmetric.add(candidate_08_0);
        expectedRankingAsymmetric.add(candidate_02_2);
        expectedRankingAsymmetric.add(candidate_07_0);
        expectedRankingAsymmetric.add(candidate_01_2);
        expectedRankingAsymmetric.add(candidate_05_1);

        List<Candidate> actualRankingAsymmetric = ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 10);

        assertArrayEquals(actualRankingAsymmetric.toArray(), expectedRankingAsymmetric.toArray());

    }

    @Test
    public void testBuildFairRanking_asymmetricTreeMostUnlikely() {
        /**
         * ASYMMETRIC TREE most unlikely path: Expected Ranking:
         * NP|NP|P1|P1|P2|NP|NP|P2|NP|P1
         */

        MultinomialFairRanker ranker = new MultinomialFairRanker(10, new double[] { 2.0 / 5.0, 1.0 / 5.0, 2.0 / 5.0 },
                0.1, false, this.unfairRanking);

        List<Candidate> expectedRankingAsymmetric = new ArrayList<>();

        expectedRankingAsymmetric.add(candidate_11_0);
        expectedRankingAsymmetric.add(candidate_10_0);
        expectedRankingAsymmetric.add(candidate_06_1);
        expectedRankingAsymmetric.add(candidate_05_1);
        expectedRankingAsymmetric.add(candidate_03_2);
        expectedRankingAsymmetric.add(candidate_09_0);
        expectedRankingAsymmetric.add(candidate_08_0);
        expectedRankingAsymmetric.add(candidate_02_2);
        expectedRankingAsymmetric.add(candidate_07_0);
        expectedRankingAsymmetric.add(candidate_04_1);

        List<Candidate> actualRankingAsymmetric = ranker.buildFairRanking(FairRankingStrategy.MOST_UNLIKELY, 10);

        assertArrayEquals(actualRankingAsymmetric.toArray(), expectedRankingAsymmetric.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildFairRanking_NotEnoughProtected() {
        // remove entire group 1
        this.unfairRanking.removeAll(Arrays.asList(candidate_06_1, candidate_04_1, candidate_05_1));
        MultinomialFairRanker ranker = new MultinomialFairRanker(10, new double[] { 2.0 / 5.0, 1.0 / 5.0, 2.0 / 5.0 },
                0.1, false, this.unfairRanking);
        ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildFairRanking_NoCandidates() {
        // remove all candidates
        this.unfairRanking.removeAll(unfairRanking);
        MultinomialFairRanker ranker = new MultinomialFairRanker(10, new double[] { 2.0 / 5.0, 1.0 / 5.0, 2.0 / 5.0 },
                0.1, false, this.unfairRanking);
        ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 8);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildFairRanking_NotEnoughCandidates() {
        // remove all candidates
        this.unfairRanking.removeAll(unfairRanking);
        MultinomialFairRanker ranker = new MultinomialFairRanker(10, new double[] { 2.0 / 5.0, 1.0 / 5.0, 2.0 / 5.0 },
                0.1, false, this.unfairRanking);
        ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 15);
    }

    @Test
    public void testBuildFairRanking_MirroredNodes() {
        fail("Not yet implemented");
    }

}
