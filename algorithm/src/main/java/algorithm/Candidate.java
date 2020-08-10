package algorithm;

import java.util.UUID;

public class Candidate implements Comparable<Candidate> {
    private UUID uuid;
    private Double score;
    private Double originalScore;
    private int group;

    public Candidate(Double score, int group) {
        this.uuid = UUID.randomUUID();
        this.score = score;
        this.originalScore = score;
        this.group = group;
    }

    private Candidate(UUID uuid, Double score, Double originalScore, int group) {
        this.uuid = uuid;
        this.score = score;
        this.originalScore = originalScore;
        this.group = group;
    }

    public Candidate(Candidate candidate) {
        this(candidate.getUuid(), candidate.getScore(), candidate.getOriginalScore(), candidate.getGroup());
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
        return cand.getScore().compareTo(this.score);
    }
    
    public String toString() {
        return uuid.toString() + ", " + score.toString() + ", " + group + "\n";
    }

}
