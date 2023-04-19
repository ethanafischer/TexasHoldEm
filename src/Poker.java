import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Poker {
    public static void main(String[] args) {
        long startTime = TexasHoldEm();
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double seconds = (double) elapsedTime / 1_000_000_000.0;
        System.out.println("__________________________________________________");
        System.out.println("Program runtime: " + seconds + " seconds");
    }

    public static long TexasHoldEm() {
        int[] play = startGame();
        long startTime = System.nanoTime();
        int players = play[0];
        int rounds = play[1];

        // some stress testing data. can calculate over 100,000 individual hands in 30 seconds!
        // 9 players, 1,000,000 hands: 322.45 seconds
        // rough runtime: 0.3 ms per hand!
        Map<HandRanking, Integer> handFreq = new HashMap<>();
        for (HandRanking hr: HandRanking.values()) {
            handFreq.put(hr, 0);
        }

        for(int i = 0; i < rounds; i++){
            System.out.printf("Hand %s:\n", i+1);
            HandRanking winningHand = PlayHand(players);
            handFreq.put(winningHand, handFreq.get(winningHand) + 1);
        }

        // Print the frequency of each HandRanking
        System.out.printf("Frequency of winning hand rankings (%s players):\n", players);
        for (HandRanking hr : HandRanking.values()) {
            int wins = handFreq.get(hr);
            double winRate = ((double) wins / rounds) * 100;
            System.out.printf("%-15s: %.2f%% (%d/%d)\n", hr.getName(), winRate, wins, rounds);
        }
        return startTime;
    }

    public static HandRanking PlayHand(int n) {
        Deck deck = new Deck();
        List<EvalTuple> evals = new ArrayList<>();
        Player[] players = new Player[n];
        Card[] community = new Card[5];
        deal(players, community, deck);
        print(players, community);

        System.out.println("Calculating hands...");
        for(int i = 0; i < n; i++){ //evaluate each hand
            Card[] hand = new Card[2 + community.length];
            System.arraycopy(players[i].getHand().getCards(), 0, hand, 0, 2);
            System.arraycopy(community, 0, hand, 2, community.length); //combine community + player cards

            System.out.printf("%s: ", players[i].getName());
            evals.add(evaluate(hand));
        }

        int[] winner = compareHands(evals);
        System.out.println();

        if (winner.length == 1 && winner[0] != -1) {
            System.out.printf("Player %s wins. \n%s", winner[0]+1, printHand(evals.get(winner[0])));
        } else {
            String winList = Arrays.stream(winner)
                    .map(i -> i + 1)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(", "));
            System.out.printf("Chop pot. \nPlayers %s have %s", winList, printHand(evals.get(winner[0])));
        }
        System.out.println("__________________________________________________");
        System.out.println();
        return evals.get(winner[0]).getHr();
    }

    public static int[] startGame(){
        Scanner sc = new Scanner(System.in);
        int n, m;

        while (true) {
            System.out.print("How many players (1-22): ");
            n = sc.nextInt();
            if (n >= 1 && n <= 22) {
                break;
            } else {
                System.out.println("Not valid input.");
            }
        }

        System.out.print("How many rounds do you want to play? ");
        m = sc.nextInt();

        System.out.println("Begin.\n");
        return new int[]{n,m};
    }

    public static void deal(Player[] players, Card[] community, Deck deck){
        //the hand is dealt in a circular fashion. the second card is not dealt
        //until all players have received their first card
        Hand[] hands = new Hand[players.length];
        for(int i = 0; i < players.length; i++){
            hands[i] = new Hand();
            hands[i].addCard(deck.nextCard()); //first card
            players[i] = new Player("Player " + (i+1), hands[i]);
        }
        for(int j = 0; j < players.length; j++){
            hands[j].addCard(deck.nextCard()); //second card
        }

        //community cards
        deck.nextCard(); //burn
        community[0] = deck.nextCard(); //flop
        community[1] = deck.nextCard();
        community[2] = deck.nextCard();
        deck.nextCard(); //burn
        community[3] = deck.nextCard(); //turn
        deck.nextCard(); //burn
        community[4] = deck.nextCard();
    }

    public static int[] compareHands(List<EvalTuple> evals) {
        int[] winner; // compareHands returns an array of player(s) who won the hand.

        // to determine the winner of the hand,
        // first, compare HandRanking values. if there is a tie for strongest hand, then
        // second, compare ranks that make up that hand.  if there is still a tie, then
        // third, compare kicker cards. if there is still a tie, then
        // return the players who chop the pot.

        // compareHR takes the player (represented by a number from 1-n), and the strength of their hand
        List<Integer[]> compareHR = new ArrayList<>(IntStream.range(0, evals.size())
                .mapToObj(i -> new Integer[]{i + 1, evals.get(i).getHr().getValue()})
                .sorted(Comparator.<Integer[]>comparingInt(x -> x[1]).reversed()).toList());

        // remove all hands weaker than the strongest hand
        int maxVal = compareHR.get(0)[1];
        compareHR.removeIf(x -> x[1] != maxVal);

        // we can finish early if we have already determined a winner:
        if(compareHR.size() == 1) {
            return new int[]{compareHR.get(0)[0]-1};
        }

        // compare ranks. the index of compareRanks represents the player number.
        // the arrays are value representations of the ranks in the players' hands
        // since the index matters, we can't use a List<Integer[]> and call .remove().
        // instead, to represent removing a player, we replace the element with an empty array.
        int[][] compareRanks = new int[evals.size()][7]; // highest possible ranks array length. e.g. 7 card flush
        for (Integer[] integers : compareHR) {
            Rank[] ranks = evals.get(integers[0]-1).getRanks();
            for(int i = 0; i < ranks.length; i++){
                compareRanks[integers[0]-1][i] = ranks[i].getValue();
            }
        }

        compareArrays(compareRanks);

        // check if compareRanks has only one player. need to do stream since there are still empty arrays
        if(Arrays.stream(compareRanks)
                .filter(a -> a.length != 0)
                .count() == 1) {
            winner = IntStream.range(0, compareRanks.length)
                    .filter(i -> compareRanks[i].length != 0)
                    .toArray();
            return winner;
        }

        // compare kicker cards
        int[][] compareKickers = new int[evals.size()][4]; // highest possible kicker array length

        for (int i = 0; i < compareRanks.length; i++) {
            if (compareRanks[i].length != 0) {
                Rank[] kickers = evals.get(i).getKicker();
                for (int j = 0; j < kickers.length; j++) {
                    compareKickers[i][j] = kickers[j].getValue();
                }
            }
        }

        // before comparing kickers, we first check to see if the array has only 0s. if so, it is a tie.
        boolean isNullArr = true;
        for (int[] row : compareKickers) {
            for (int val : row) {
                if (val != 0) {
                    isNullArr = false;
                    break;
                }
            }
        }

        if(!isNullArr) compareArrays(compareKickers); // if there are kickers, compare them.
        else {
            winner = IntStream.range(0, compareRanks.length) //if there aren't any kickers, return chop pot.
                    .filter(i -> compareRanks[i].length != 0)
                    .toArray();
            return winner;
        }

        // returns either the player with the highest kicker, or the players in a chop pot.
        winner = IntStream.range(0, compareKickers.length)
                .filter(i -> compareKickers[i].length != 0)
                .toArray();

        return winner;
    }

    public static void compareArrays(int[][] arr) {
        int[] maxArray = arr[0];
        List<Integer> prevMaxIndex = new ArrayList<>();
        prevMaxIndex.add(0);
        for(int i = 0; i < maxArray.length; i++) {
            for (int j = 1; j < arr.length; j++) {
                int[] cur = arr[j];
                if(cur.length != 0) { //null check
                    if (cur[i] == maxArray[i]) { // two players are currently tied for max rank
                        prevMaxIndex.add(j); // add player to list of max ranks
                    } else if (cur[i] > maxArray[i]) { // the current player has a better ranked card
                        for (int idx : prevMaxIndex) { // set previous max(es) to empty array(s)
                            if (idx != j) { // to ensure the current player is not removed
                                arr[idx] = new int[0]; // players that previously had the best rank are now removed
                            }
                        }
                        prevMaxIndex.clear();
                        prevMaxIndex.add(j); // update previous max index
                        maxArray = cur; // update max array
                    } else {
                        arr[j] = new int[0]; // the current player has a worse rank
                    }
                }
            }
        }

    }

    public static EvalTuple evaluate(Card[] hand){
        Arrays.sort(hand, (c1, c2) -> Integer.compare(c2.getRank().getValue(), c1.getRank().getValue())); //sort by rank
        Rank[] ranks = new Rank[7];
        for(int i = 0; i < 7; i++){
            ranks[i] = hand[i].getRank();
        }
        Arrays.sort(ranks);

        //check for all possible hands
        HandTuple straightFlushCheck = hasStraightFlush(hand);
        HandTuple flushCheck = hasFlush(hand);
        HandTuple straightCheck = hasStraight(ranks);
        HandTuple fourCheck = hasFourOfAKind(ranks);
        HandTuple tripsCheck = hasThreeOfAKind(ranks);
        HandTuple pairCheck = hasPair(ranks);
        HandTuple boatCheck = hasFullHouse(tripsCheck.getRanks(), pairCheck.getRanks());

        // for readability's sake
        boolean isStraightFlush = straightFlushCheck.getBool();
        boolean isFlush = flushCheck.getBool();
        boolean isStraight = straightCheck.getBool();
        boolean isFourOfAKind = fourCheck.getBool();
        boolean isFullHouse = boatCheck.getBool();
        boolean isThreeOfAKind = tripsCheck.getBool();
        boolean isTwoPair = pairCheck.getBool() && pairCheck.getRanks().length == 2;
        boolean isPair = pairCheck.getBool() && pairCheck.getRanks().length == 1;

        EvalTuple eval = new EvalTuple(null, new Rank[0], new Rank[0]);

        // checking for existence of a given hand, from strongest to weakest, and updating eval if true
        if (isStraightFlush && straightFlushCheck.getRanks()[0] == Rank.ACE) {
            eval.setHr(HandRanking.ROYAL_FLUSH);
        } else if (isStraightFlush) {
            eval.setHr(HandRanking.STRAIGHT_FLUSH);
            eval.setRanks(straightFlushCheck.getRanks());
        } else if (isFourOfAKind) {
            eval.setHr(HandRanking.FOUR_OF_A_KIND);
            eval.setRanks(fourCheck.getRanks());
        } else if (isFullHouse) {
            eval.setHr(HandRanking.FULL_HOUSE);
            eval.setRanks(boatCheck.getRanks());
        } else if (isFlush) {
            eval.setHr(HandRanking.FLUSH);
            eval.setRanks(flushCheck.getRanks());
        } else if (isStraight) {
            eval.setHr(HandRanking.STRAIGHT);
            eval.setRanks(straightCheck.getRanks());
        } else if (isThreeOfAKind) {
            eval.setHr(HandRanking.THREE_OF_A_KIND);
            eval.setRanks(tripsCheck.getRanks());
            eval.setKicker(tripsCheck.getKicker());
        } else if (isTwoPair) {
            eval.setHr(HandRanking.TWO_PAIR);
            eval.setRanks(new Rank[]{pairCheck.getRanks()[0], pairCheck.getRanks()[1]});
            eval.setKicker(pairCheck.getKicker());
        } else if (isPair) {
            eval.setHr(HandRanking.ONE_PAIR);
            eval.setRanks(new Rank[]{pairCheck.getRanks()[0]});
            eval.setKicker(pairCheck.getKicker());
        } else {
            eval.setHr(HandRanking.HIGH_CARD);
            eval.setRanks(new Rank[]{ranks[0]});
            eval.setKicker(new Rank[]{ranks[1], ranks[2], ranks[3], ranks[4]});
        }

        System.out.print(printHand(eval));
        return eval;
    }

    private static HandTuple hasStraightFlush(Card[] hand){
        HandTuple hasStraightFlush = new HandTuple(false, null, null);
        HandTuple f = hasFlush(hand); //check if there is a flush

        if(f.getBool()) {
            HandTuple r = hasStraight(f.getRanks()); //check if there is a straight with these flush cards
            if(r.getBool()) { //there is a straight flush
                hasStraightFlush.setBool(true);
                hasStraightFlush.setRanks(r.getRanks());
            }
        }
        return hasStraightFlush;
    }

    private static HandTuple hasStraight(Rank[] ranks){
        HandTuple straight = new HandTuple(false, null, null);

        //remove duplicates from ranks array
        Set<Rank> uniqueRanks = new HashSet<>(Arrays.asList(ranks));
        Rank[] noDupes = uniqueRanks.toArray(new Rank[0]);
        Arrays.sort(noDupes);

        if (noDupes.length <= 4){
            return straight; // no straights available.
        }

        //Ace is a special case since it can be the high or low end of a straight.
        if(noDupes[0] == Rank.ACE){
            List<Rank> list = Arrays.asList(noDupes);
            // check if there is 2-5.
            if (list.contains(Rank.FIVE) &&
                    list.contains(Rank.FOUR) &&
                    list.contains(Rank.THREE) &&
                    list.contains(Rank.TWO) &&
                    !list.contains(Rank.SIX)) {
                // 5-A straight
                straight.setBool(true); //there is a straight
                straight.setRanks(new Rank[]{Rank.FIVE, Rank.ACE});
                return straight;
            }
        }

        for(int i = 0; i <= noDupes.length-5; i++){
            // iterate through the ranks. if there is a straight,
            // it must begin at one of these first ranks and none after.
            if (noDupes[i].getValue() - noDupes[i+4].getValue() == 4){
                straight.setBool(true); //there is a straight
                straight.setRanks(new Rank[]{noDupes[i], noDupes[i+4]});
                return straight; // to account for 6 or 7 card straights.
            }
        }
        return straight;
    }

    private static HandTuple hasFlush(Card[] hand){
        HandTuple flush = new HandTuple(false, null, null);

        Map<Suit, Integer> suitCount = new HashMap<>();
        for (Card c: hand) {
            Suit s = c.getSuit();
            int count = suitCount.getOrDefault(s, 0);
            suitCount.put(s, count+1);
        }

        List<Card> f = new ArrayList<>();
        for (Card c: hand) {
            if (suitCount.get(c.getSuit()) >= 5) {
                flush.setBool(true);
                f.add(c);
            }
        }

        if (f.size() >= 5) { //there is a flush.
            Rank[] ranks = new Rank[f.size()];
            for(int i = 0; i < f.size(); i++){
                ranks[i] = f.get(i).getRank();
            }
            flush.setRanks(ranks); //cards in the flush
        }

        return flush;
    }

    private static HandTuple hasFourOfAKind(Rank[] ranks){
        HandTuple fourOfAKind = new HandTuple(false, null, null);
        Map<Rank, Integer> rankCounts = new HashMap<>();

        for (Rank r: ranks) {
            int count = rankCounts.getOrDefault(r, 0) + 1;
            if (count == 4) {
                fourOfAKind.setBool(true);
                fourOfAKind.setRanks(new Rank[]{r});
                return fourOfAKind;
            }
            rankCounts.put(r, count);
        }

        return fourOfAKind;
    }

    private static HandTuple hasFullHouse(Rank[] trips, Rank[] pair) {
        HandTuple fullHouse = new HandTuple(false, null, null);
        List<Rank> boatRanks = new ArrayList<>(2);

        if(trips.length == 0 && pair.length == 0) { //edge cases: no trips or pairs
            return fullHouse;
        } else if (trips.length == 0){
            return fullHouse;
        } else if (trips.length == 1 && pair.length == 1){ //only one possible boat
            fullHouse.setBool(true);
            boatRanks.add(trips[0]);
            boatRanks.add(pair[0]);
            fullHouse.setRanks(boatRanks.toArray(new Rank[0]));
            return fullHouse;
        }

        // cases where there is a combination of multiple trips or 1 trips and multiple pairs.
        boatRanks.add(trips[0]);
        if (pair.length == 0) {
            try {   // this is for very few cases, like quads over full house.  The alternative is to
                // pass in fourCheck, which is kind of unnecessary.
                boatRanks.add(trips[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                return fullHouse;
            }
        } else {
            boatRanks.add(pair[0]);
        }

        fullHouse.setBool(true);
        fullHouse.setRanks(boatRanks.toArray(new Rank[0]));

        return fullHouse;
    }

    private static HandTuple hasThreeOfAKind(Rank[] ranks) {
        boolean tripsBool = false;
        HandTuple threeOfAKind = new HandTuple(false, null, null);
        Map<Rank, Integer> rankCounts = new LinkedHashMap<>(); // necessary to keep order of ranks

        for (Rank r: ranks) {
            int count = rankCounts.getOrDefault(r, 0) + 1;
            rankCounts.put(r, count);
        }

        //necessary because there could be multiple three of a kinds.
        List<Rank> tripsRanks = new ArrayList<>();
        for(Map.Entry<Rank, Integer> entry: rankCounts.entrySet()) {
            Rank r = entry.getKey();
            int count = entry.getValue();
            if (count == 3) {
                tripsBool = true;
                tripsRanks.add(r);
            }
        }

        threeOfAKind.setBool(tripsBool);
        threeOfAKind.setRanks(tripsRanks.toArray(new Rank[0]));
        if(threeOfAKind.getBool()) threeOfAKind.setKicker(kicker(threeOfAKind, ranks, 1));

        return threeOfAKind;
    }

    private static HandTuple hasPair(Rank[] ranks) {
        boolean pairBool = false;
        HandTuple pair = new HandTuple(false, null, null);
        Map<Rank, Integer> rankCounts = new LinkedHashMap<>(); //necessary to keep the order of the ranks

        for (Rank r: ranks) {
            int count = rankCounts.getOrDefault(r, 0) + 1;
            rankCounts.put(r, count);
        }

        //there could be multiple pairs.
        List<Rank> pairRanks = new ArrayList<>();
        for(Map.Entry<Rank, Integer> entry: rankCounts.entrySet()) {
            Rank r = entry.getKey();
            int count = entry.getValue();
            if (count == 2) {
                pairBool = true;
                if(pairRanks.size() < 2){
                    pairRanks.add(r); //there will only be 1 or 2 pair, not 3
                }
            }
        }

        pair.setBool(pairBool);
        pair.setRanks(pairRanks.toArray(new Rank[0]));
        if(pair.getBool()) pair.setKicker(kicker(pair, ranks, 0));

        return pair;
    }

    private static Rank[] kicker(HandTuple rt, Rank[] ranks, int type){
        //type is to designate pair or trips. it is either 0 (pair) or 1 (trips)
        if(rt.getRanks().length == 2 && type == 1) {
            return null;
        }

        List<Integer> temp = new ArrayList<>();
        List<Rank> kick = new ArrayList<>();
        for(int i = 0; i < rt.getRanks().length; i++){
            temp.add(rt.getRanks()[i].getValue());
        }

        for(Rank r: ranks){
            if(!temp.contains(r.getValue())){
                if(kick.size() == 3-type){
                    break;
                }
                kick.add(r);
            }
        }

        if(rt.getRanks().length == 2){
            kick.remove(2);
            kick.remove(1);
        }

        return kick.toArray(new Rank[0]);
    }

    public static void print(Player[] players, Card[] community){
        int count = 0;
        for(Player p: players){
            count++;
            System.out.printf("Player %s has hole cards: %s\n", count, Arrays.toString(p.getHand().getCards()));
        }
        System.out.printf("Community cards: %s\n\n", Arrays.toString(community));
    }

    public static String printHand(EvalTuple eval) {
        HandRanking hr = eval.getHr();

        String[] ranks = Arrays.stream(eval.getRanks())
                .map(Enum::toString)
                .toArray(String[]::new);

        String kicker = "";
        if(eval.getKicker().length != 0) {
            kicker = Arrays.stream(eval.getKicker())
                    .map(Rank::toString)
                    .collect(Collectors.joining(", "));
        }

        Function<String, String> pluralize = (rank) -> rank.equals("Six") ? "Sixe" : rank;

        if (hr == HandRanking.ROYAL_FLUSH) {
            return "Royal Flush";
        } else if (hr == HandRanking.STRAIGHT_FLUSH) {
            return String.format("Straight Flush, %s to %s\n", ranks[0], ranks[1]);
        } else if (hr == HandRanking.FOUR_OF_A_KIND) {
            return String.format("Four of a Kind, %ss\n", pluralize.apply(ranks[0]));
        } else if (hr == HandRanking.FULL_HOUSE) {
            return String.format("Full House, %ss full of %ss\n", pluralize.apply(ranks[0]), pluralize.apply(ranks[1]));
        } else if (hr == HandRanking.FLUSH) {
            return String.format("Flush, %s high\n", pluralize.apply(ranks[0]));
        } else if (hr == HandRanking.STRAIGHT) {
            return String.format("Straight, %s to %s\n", ranks[0], ranks[1]);
        } else if (hr == HandRanking.THREE_OF_A_KIND) {
            return String.format("Three of a Kind, %ss. Kicker: %s\n", pluralize.apply(ranks[0]), kicker);
        } else if (hr == HandRanking.TWO_PAIR) {
            return String.format("Two Pair, %ss and %ss. Kicker: %s\n", pluralize.apply(ranks[0]), pluralize.apply(ranks[1]), kicker);
        } else if (hr == HandRanking.ONE_PAIR) {
            return String.format("Pair, %ss. Kicker: %s\n", pluralize.apply(ranks[0]), kicker);
        } else {
            return String.format("High Card, %s. Kicker: %s\n", ranks[0], kicker);
        }
    }
}
