import java.util.*;

public class Hand {
    private ArrayList<Card> cards;

    public Hand() {
        this.cards = new ArrayList<Card>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }
    public Card[] getCards(){
        return cards.toArray(new Card[2]);
    }
}