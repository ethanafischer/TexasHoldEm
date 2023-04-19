public class EvalTuple {
    private HandRanking hr;
    private Rank[] ranks;
    private Rank[] kicker;

    public EvalTuple(HandRanking hr, Rank[] ranks, Rank[] kicker){
        this.hr = hr;
        this.ranks = ranks;
        this.kicker = kicker;
    }

    public HandRanking getHr() {
        return hr;
    }

    public Rank[] getRanks() {
        return ranks;
    }

    public Rank[] getKicker() { return kicker; }

    public void setHr(HandRanking hr) {
        this.hr = hr;
    }

    public void setRanks(Rank[] ranks) { this.ranks = ranks; }

    public void setKicker(Rank[] kicker) {
        this.kicker = kicker;
    }
}