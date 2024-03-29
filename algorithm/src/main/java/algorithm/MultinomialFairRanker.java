package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import algorithm.MTree.FairRankingStrategy;

public class MultinomialFairRanker {

    private MTree mTree;
    private HashMap<Integer, List<Candidate>> groupLists;
    private HashMap<Integer, Integer> numberOfCandidatesInResultPerGroup;

    public MultinomialFairRanker(int k, double[] p, double alpha, boolean doAdjust, List<Candidate> unfairRanking) {
        System.out.print('\n'+"Started building MTree for: k="+k+", p="+Arrays.toString(p)+", alpha="+alpha);
        this.mTree = new MTree(k, p, alpha, doAdjust);
        System.out.print('\n'+"Finished building MTree"+'\n');
        // create separate lists of candidates per group, with candidates sorted by
        // score
        this.groupLists = new HashMap<>();
        for (int groupID = 0; groupID < p.length; groupID++) {
            List<Candidate> groupList = new ArrayList<>();
            for (Candidate candidate : unfairRanking) {
                if (candidate.getGroup() == groupID) {
                    groupList.add(candidate);
                }
            }
            this.groupLists.put(groupID, groupList);
        }

        this.numberOfCandidatesInResultPerGroup = new HashMap<>();
        for (Integer key : groupLists.keySet()) {
            this.numberOfCandidatesInResultPerGroup.put(key, 0);
        }
    }

    /**
     * @param strategy: enum to choose from different ranking strategies --
     *        MOST_LIKELY = child node with highest mcdf MOST_UNLIKELY = child with
     *        lowest mcdf (still valid though) RANDOM = pick random child node
     * @param length: length of the output ranking
     * @return
     */
    public List<Candidate> buildFairRanking(FairRankingStrategy strategy, Integer length) {
        List<Candidate> result = new ArrayList<>(Arrays.asList(new Candidate[length]));
        List<Integer> parent = this.mTree.getRoot();
        for (int k = 0; k < length; k++) {
            // assert that there are actually any candidates left to be ranked
            if (!anyCandidatesLeft()) {
                throw new IllegalArgumentException("Not enough candidates to create ranking of size " + length);
            }
            // get the next mNode in mTree, that maps the strategy
            // as their may be several possibilities when the tree is symmetric, take the one most fitting with the
            // given strategy
            List<Integer> mNode = this.mTree.getCorrectChildNode(strategy, parent);
            boolean candidateAdded = false;
            // start from index 1, because first entry in mNode is current ranking position
            for (int groupID = 1; groupID < mNode.size(); groupID++) {
                // check how many candidates of each group are needed at least at current k
                Integer expected = mNode.get(groupID);
                Integer actual = this.numberOfCandidatesInResultPerGroup.get(groupID);
                if (expected > actual) {
                    // check if multinomial ranked group fairness criteria is met,
                    // if not add candidate from group where is missing
                    // always use first candidate, because this is the best one in terms of scores
                    try {
                        Candidate candidate = groupLists.get(groupID).get(0);
                        result.set(k, candidate);
                        candidateAdded = true;
                        this.numberOfCandidatesInResultPerGroup.put(groupID, actual + 1);
                        // do not put the same candidate twice, therefore remove from pool
                        groupLists.get(groupID).remove(0);
                        // no need to add more than one candidates at the same k
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        // if a candidate from groupID should be put, but no more candidates from this
                        // group are available, a fair ranking cannot be created
                        throw new IllegalArgumentException("Not enough candidates in group " + groupID
                                + " to create fair ranking with parameters k=" + this.mTree.getK() + ", p="
                                + this.mTree.getP() + ", and alpha=" + this.mTree.getAlpha());
                    }
                }
            }
            if (!candidateAdded) {
                // if ranked group fairness condition is met, add the candidate with the highest
                // score
                Candidate bestCandidate = new Candidate(0.0, 0, UUID.randomUUID());
                for (int groupID = 0; groupID < mNode.size(); groupID++) {
                    double currentGroupsBestScore = 0.0;
                    try {
                        currentGroupsBestScore = groupLists.get(groupID).get(0).getScore();
                    } catch (IndexOutOfBoundsException e) {
                        // we have to handle the case that one group list is already empty,
                        // because we have ranked all its candidates
                        System.out.println("All candidates of group " + groupID + " are already ranked.");
                        continue;
                    }
                    if (bestCandidate.getScore() < currentGroupsBestScore) {
                        bestCandidate = groupLists.get(groupID).get(0);
                    }
                    if (bestCandidate.getScore() == currentGroupsBestScore) {
                        // if the two candidates have the same score, pick one of them at random 
                        // to avoid preference of lower groupIDs
                        if (new Random().nextBoolean()) {
                            bestCandidate = groupLists.get(groupID).get(0);
                        }
                    }
                }
                result.set(k, bestCandidate);
                groupLists.get(bestCandidate.getGroup()).remove(0);
            }
            parent = mNode;
        }
        return result;
    }

    private boolean anyCandidatesLeft() {
        for (List<Candidate> groupList : groupLists.values()) {
            if (!groupList.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
