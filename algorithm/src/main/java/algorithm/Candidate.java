package algorithm;

import java.util.UUID;

public class Candidate implements Comparable<Candidate> {
    private UUID uuid;
    private Double score;
    private Double originalScore;
    private int group;

    public Candidate(Double score, int group, UUID uuid) {
        this.uuid = uuid;
        this.score = score;
        this.originalScore = score;
        this.group = group;
    }    

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getScore() {
        return score;
    }
    
    public UUID getUuid() {
        return uuid;
    }

    public Double getOriginalScore() {
        return this.originalScore;
    }

    public int getGroup() {
        return group;
    }
    
    @Override
    public int compareTo(Candidate cand) {
        // sort scores in descending order
        int compareResult = cand.getScore().compareTo(this.score);
        if (compareResult == 0) {
            // second level sort uuid in ascending order
            return this.uuid.compareTo(cand.getUuid());
        } else {
            return compareResult;
        }
    }
    
    public String toString() {
        return uuid.toString() + ", " + score.toString() + ", " + group + "\n";
    }

}
