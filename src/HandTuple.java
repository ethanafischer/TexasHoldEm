public class HandTuple {
    private boolean bool;
    private Rank[] ranks;
    private Rank[] kicker;

    public HandTuple(boolean bool, Rank[] ranks, Rank[] kicker){
        this.bool = bool; // does the hand exist?
        this.ranks = ranks; //if so, what is the rank.
        this.kicker = kicker; //for tiebreak hands, what is the kicker? in some cases also represents suit for flush.
    }

    public boolean getBool() { return bool; }

    public Rank[] getRanks() { return ranks; }
    public Rank[] getKicker() { return kicker; }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public void setRanks(Rank[] ranks) {
        this.ranks = ranks;
    }

    public void setKicker(Rank[] kicker) { this.kicker = kicker; }
}
