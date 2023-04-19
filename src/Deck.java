import java.util.*;

public class Deck {
    private ArrayList<Card> deck = new ArrayList<Card>(52);

    public Deck(){
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck);
    }

    public void print(){
        System.out.println(deck);
    }

    public Card nextCard(){
        return deck.remove(0);
    }
}
