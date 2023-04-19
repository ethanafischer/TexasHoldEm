public enum Suit {
    CLUBS("\u2663"),
    DIAMONDS("\u2666"),
    HEARTS("\u2665"),
    SPADES("\u2660");

    private final String name;

    Suit(String name) {
        this.name = name;
    }

    public String toString() { return name; }
}