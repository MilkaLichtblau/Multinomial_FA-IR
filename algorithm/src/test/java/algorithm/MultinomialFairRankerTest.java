package algorithm;

import static org.junit.Assert.*;

import algorithm.Candidate;
import algorithm.MultinomialFairRanker;
import algorithm.MultinomialFairRanker.FairRankingStrategy;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MultinomialFairRankerTest {

    private MultinomialFairRanker ranker;
    private List<Candidate> unfairRanking;

    @Before
    public void setup() {
        int k = 9;
        double[] p = {1.0/3.0, 1.0/3.0, 1.0/3.0};
        double alpha = 0.1;
        
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
        
        this.ranker = new MultinomialFairRanker(k,p,alpha, true, this.unfairRanking);
    }

    @Test
    public void testBuildFairRanking() {
        /**
         *  Expected Ranking: NP|NP|P1|P2|NP|P1|P2|NP|P1
         */

        List<Candidate> expectedRanking = new ArrayList<>();
        expectedRanking.add(unfairRanking.get(0));
        expectedRanking.add(unfairRanking.get(1));
        expectedRanking.add(unfairRanking.get(4));
        expectedRanking.add(unfairRanking.get(7));
        expectedRanking.add(unfairRanking.get(2));
        expectedRanking.add(unfairRanking.get(5));
        expectedRanking.add(unfairRanking.get(8));
        expectedRanking.add(unfairRanking.get(3));
        expectedRanking.add(unfairRanking.get(6));

        List<Candidate> actualRanking = this.ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, 9);

        assertArrayEquals(actualRanking.toArray(),expectedRanking.toArray());
    }

}
