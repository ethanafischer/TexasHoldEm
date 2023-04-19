public enum HandRanking {
    ROYAL_FLUSH("Royal Flush", 10),
    STRAIGHT_FLUSH("Straight Flush", 9),
    FOUR_OF_A_KIND("Four of a Kind", 8),
    FULL_HOUSE("Full House", 7),
    FLUSH("Flush", 6),
    STRAIGHT("Straight", 5),
    THREE_OF_A_KIND("Three of a Kind", 4),
    TWO_PAIR("Two Pair", 3),
    ONE_PAIR("One Pair", 2),
    HIGH_CARD("High Card", 1);

    private final String name;
    private final int value;
    HandRanking(String name, int value){
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }

    public int getValue() { return value;}
}
