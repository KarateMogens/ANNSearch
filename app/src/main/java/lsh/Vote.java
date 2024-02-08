package lsh;

public class Vote {

    private int cIndex;
    private int votes;

    public int getcIndex() {
        return cIndex;
    }

    public int getVotes() {
        return votes;
    }

    public Vote(int cIndex, int votes) {
        this.cIndex = cIndex;
        this.votes = votes;
    }

}