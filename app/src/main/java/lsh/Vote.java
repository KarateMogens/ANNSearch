package lsh;

public class Vote {

    private int cIndex;
    private float votes;

    public int getcIndex() {
        return cIndex;
    }

    public float getVotes() {
        return votes;
    }

    public Vote(int cIndex, float votes) {
        this.cIndex = cIndex;
        this.votes = votes;
    }

}