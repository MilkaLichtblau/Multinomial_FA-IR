package algorithm;

public class Candidate {

    private double score;
    private double originalScore;
    private int group;

    public Candidate(double score, int group) {
        this.score = score;
        this.originalScore = score;
        this.group = group;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public double getOriginalScore() {
        return this.originalScore;
    }

    public int getGroup() {
        return group;
    }

}
