package algorithm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MultinomialFairRankerTest {

    private MultinomialFairRanker ranker;

    @Before
    public void setup() {
        int k = 9;
        double[] p = {1.0/3.0, 1.0/3.0, 1.0/3.0};
        double alpha = 0.1;
        this.ranker = new MultinomialFairRanker(k,p,alpha);
    }

    @Test
    public void testMTree() {
        fail("Not yet implemented");
    }

    @Test
    public void testBuildFairRanking() {
        /**
         *  Expected Ranking: NP|NP|P1|P2|NP|P1|P2|NP|P1
         */

        List<Candidate> unprotectedGroup = new ArrayList<>();
        unprotectedGroup.add(new Candidate(10.0, 0));
        unprotectedGroup.add(new Candidate(9.0, 0));
        unprotectedGroup.add(new Candidate(8.0, 0));
        unprotectedGroup.add(new Candidate(7.0, 0));

        List<Candidate> protectedGroup1 = new ArrayList<>();
        protectedGroup1.add(new Candidate(6.0, 1));
        protectedGroup1.add(new Candidate(5.0, 1));
        protectedGroup1.add(new Candidate(4.0, 1));

        List<Candidate> protectedGroup2 = new ArrayList<>();
        protectedGroup2.add(new Candidate(3.0, 2));
        protectedGroup2.add(new Candidate(2.0, 2));

        List<Candidate> expectedRanking = new ArrayList<>();
        expectedRanking.add(unprotectedGroup.get(0));
        expectedRanking.add(unprotectedGroup.get(1));
        expectedRanking.add(protectedGroup1.get(0));
        expectedRanking.add(protectedGroup2.get(0));
        expectedRanking.add(unprotectedGroup.get(2));
        expectedRanking.add(protectedGroup1.get(1));
        expectedRanking.add(protectedGroup2.get(1));
        expectedRanking.add(unprotectedGroup.get(3));
        expectedRanking.add(protectedGroup1.get(2));

        List<List<Candidate>> groupLists = new ArrayList<>();
        groupLists.add(unprotectedGroup);
        groupLists.add(protectedGroup1);
        groupLists.add(protectedGroup2);
        List<Candidate> actualRanking = this.ranker.buildFairRanking(groupLists);

        assertArrayEquals(actualRanking.toArray(),expectedRanking.toArray());
    }

}
