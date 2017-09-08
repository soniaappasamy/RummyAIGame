import java.util.Collections;
import java.util.ArrayList;
import java.util.Stack;

public class GameState {
	
	private static final int HEARTS = 0;
	private static final int DIAMONDS = 1;
	private static final int CLUBS = 2;
	private static final int SPADES = 3;
	
	//Use numbers for suits and ranks
	private ArrayList<int[]> playerOneHand;
	private ArrayList<int[]> playerTwoHand;
	
	private int[] discardPileTop;
	private Stack<int[]> stockPile;
	public boolean hasDiscard;
	
	private int playerOneScore;
	private int playerTwoScore;
	private int playerOneHandsWon;
	private int playerTwoHandsWon;
	
	private int player_turn; // current player's turn
	
	public boolean roundEnd; // true if the round has ended (by knocking or going gin)
	
	public GameState(int playerOneScore, int playerTwoScore, int playerOneHandsWon,
			int playerTwoHandsWon, int player_turn)
	{
		this.playerOneScore = playerOneScore;
		this.playerTwoScore = playerTwoScore;
		this.playerOneHandsWon = playerOneHandsWon;
		this.playerTwoHandsWon = playerTwoHandsWon;
		this.player_turn = player_turn;
		roundEnd = false;
		hasDiscard = false;
	}
	
	/* Creates a new state that is a copy of oldstate, but with predictions for 
	 * the opponent hand and deck. This assumes the opponent is not the player
	 * whose turn it is currently.
	 */
	public GameState(GameState oldstate, 
			Stack<int[]> deck_prediction, ArrayList<int[]> opponent_prediction) {
		this(oldstate.getPlayerScore(1), oldstate.getPlayerScore(2), 
				oldstate.getHandsWon(1), oldstate.getHandsWon(2), oldstate.getTurn());
		
		// Set new stockPile and opponent hand
		stockPile = deck_prediction;
		hasDiscard = oldstate.hasDiscard;
		if(hasDiscard)
			discardPileTop = new int[] {oldstate.discardPileTop[0], oldstate.discardPileTop[1]};
		if (player_turn == 1) {
			playerTwoHand = opponent_prediction;
			playerOneHand = new ArrayList<int[]>(oldstate.playerOneHand);
		} else {
			playerOneHand = opponent_prediction;
			playerTwoHand = new ArrayList<int[]>(oldstate.playerTwoHand);
		}
		roundEnd = false;
	}
	
	/* Creates a new state that results from drawing a card the discard pile if
	 * draw_discard == true or from the stock pile if draw_discard == false,
	 * and discarding a card. If card == [0, 0], then discard same card as drawn.
	 */
	public GameState(GameState oldstate, boolean draw_discard, int[] card) {
		this(oldstate.getPlayerScore(1), oldstate.getPlayerScore(2), 
				oldstate.getHandsWon(1), oldstate.getHandsWon(2), oldstate.getTurn());
		
		hasDiscard = oldstate.hasDiscard;
		if(hasDiscard)
			discardPileTop = new int[] {oldstate.discardPileTop[0], oldstate.discardPileTop[1]};
		playerOneHand = new ArrayList<int[]>(oldstate.playerOneHand);
		playerTwoHand = new ArrayList<int[]>(oldstate.playerTwoHand);
		stockPile = new Stack<int[]>();
		stockPile.addAll(oldstate.stockPile);
		
		// Update stock pile, discard pile, and player hands
		ArrayList<int[]> hand = getMyHand(player_turn);
		if (draw_discard) {
			// Drawing from discard pile
			hand.add(discardPileTop);
			if(card == new int[]{0,0})
				card = discardPileTop;
		} else {
			// Drawing from stock pile
			int[] temp = getStockPileTop();
			hand.add(temp);
			if(card == new int[]{0,0})
				card = temp;
		}
		hand.remove(card);
		discardPileTop = card;
		nextTurn();
		roundEnd = false;
	}
	
	/** Creates a new GameState object that is a copy of oldstate, but with the
	 * round finished by knocking or going gin.
	 */
	public GameState(GameState oldstate, boolean draw_discard) {
		// Copy everything
		this(oldstate.getPlayerScore(1), oldstate.getPlayerScore(2), 
				oldstate.getHandsWon(1), oldstate.getHandsWon(2), oldstate.getTurn());
		
		hasDiscard = oldstate.hasDiscard;
		if(hasDiscard)
			discardPileTop = new int[] {oldstate.discardPileTop[0], oldstate.discardPileTop[1]};
		playerOneHand = new ArrayList<int[]>(oldstate.playerOneHand);
		playerTwoHand = new ArrayList<int[]>(oldstate.playerTwoHand);
		stockPile = new Stack<int[]>();
		stockPile.addAll(oldstate.stockPile);
		
		// Update stock pile, discard pile, and player hands
		ArrayList<int[]> hand = getMyHand(player_turn);
		if (draw_discard) {
			// Drawing from discard pile
			hand.add(discardPileTop);
		} else {
			// Drawing from stock pile
			hand.add(getStockPileTop());
		}
		
		// Discard highest valued deadwood card
		int[] drop = new int[] {0,0};
		for (int i = 0; i < 11; i++) {
			int[] card = hand.get(i);
			if (Helper.isDeadwood(card, hand) && card[0] > drop[0]) {
				drop = card;
			}
		}
		if (drop[0] != 0) {
			// Drop deadwood card
			hand.remove(drop);
			discardPileTop = drop;
		} else {
			// Does not matter which card to drop, since all are in runs/sets
			discardPileTop = hand.remove(0);
		}
		//setMyHand(player_turn, hand);
		
		endRound();
	}
	
	/** Prints out a single card in a human-readable format */
	public void prettyPrintCard(int[] card)
	{
		switch(card[0]) {
		case 1:
			System.out.print("Ace");
			break;
		case 11:
			System.out.print("Jack");
			break;
		case 12:
			System.out.print("Queen");
			break;
		case 13:
			System.out.print("King");
			break;
		default:
			System.out.print(card[0]);
		}
	
		switch(card[1]) {
		case HEARTS:
			System.out.println(" of Hearts");
			break;
		case DIAMONDS:
			System.out.println(" of Diamonds");
			break;
		case CLUBS:
			System.out.println(" of Clubs");
			break;
		default:
			System.out.println(" of Spades");
		}
	}
	
	public String imageFormatCard(int[] card)
	{
		String cardOut = "";
		switch(card[0]) {
		case 1:
			cardOut = "ace_of_";
			break;
		case 11:
			cardOut = "jack_of_";
			break;
		case 12:
			cardOut = "queen_of_";
			break;
		case 13:
			cardOut = "king_of_";
			break;
		default:
			cardOut = Integer.toString(card[0]) + "_of_";
		}
	
		switch(card[1]) {
		case HEARTS:
			cardOut = cardOut + "hearts";
			break;
		case DIAMONDS:
			cardOut = cardOut + "diamonds";
			break;
		case CLUBS:
			cardOut = cardOut + "clubs";
			break;
		default:
			cardOut = cardOut + "spades";
		}
		
		return cardOut;
	}
	
	/** Prints out a single card in a human-readable format */
	public String prettyFormatCard(int[] card)
	{
		String cardOut = "";
		switch(card[0]) {
		case 1:
			cardOut = "Ace";
			break;
		case 11:
			cardOut = "Jack";
			break;
		case 12:
			cardOut = "Queen";
			break;
		case 13:
			cardOut = "King";
			break;
		default:
			cardOut = Integer.toString(card[0]);
		}
	
		switch(card[1]) {
		case HEARTS:
			cardOut = cardOut + " of Hearts";
			break;
		case DIAMONDS:
			cardOut = cardOut + " of Diamonds";
			break;
		case CLUBS:
			cardOut = cardOut + " of Clubs";
			break;
		default:
			cardOut = cardOut + " of Spades";
		}
		
		return cardOut;
	}
	
	public String prettyFormatScoreBoard(int playerNumber)
	{
		String board = "";
		int myScore;
		int opponentScore;
		int myHandsWon;
		int opponentHandsWon;
		ArrayList<int[]> myHand;
		
		if(playerNumber == 1)
		{
			myScore = playerOneScore;
			opponentScore = playerTwoScore;
			myHandsWon = playerOneHandsWon;
			opponentHandsWon = playerTwoHandsWon;
			myHand = playerOneHand;
		}
		else
		{
			myScore = playerTwoScore;
			opponentScore = playerOneScore;
			myHandsWon = playerTwoHandsWon;
			opponentHandsWon = playerOneHandsWon;
			myHand = playerTwoHand;
		}
		
		board = "My Score: " + myScore;
		board = board + "\nOpponent's Score: " + opponentScore;
		board = board + "\nMy Hands Won: " + myHandsWon;
		board = board + "\nOpponent's Hands Won: " + opponentHandsWon;
		
		return board;
	}
	
	/** Prints out the current game state in a human-readable
	 *  form to the console, but only allowing playerNumber's
	 *  hand to be visible */
	public void prettyPrintState(int playerNumber)
	{
		int myScore;
		int opponentScore;
		int myHandsWon;
		int opponentHandsWon;
		ArrayList<int[]> myHand;
		
		if(playerNumber == 1)
		{
			myScore = playerOneScore;
			opponentScore = playerTwoScore;
			myHandsWon = playerOneHandsWon;
			opponentHandsWon = playerTwoHandsWon;
			myHand = playerOneHand;
		}
		else
		{
			myScore = playerTwoScore;
			opponentScore = playerOneScore;
			myHandsWon = playerTwoHandsWon;
			opponentHandsWon = playerOneHandsWon;
			myHand = playerTwoHand;
		}
		
		System.out.println("My Score: " + myScore);
		System.out.println("Opponent's Score: " + opponentScore);
		System.out.println("My Hands Won: " + myHandsWon);
		System.out.println("Opponent's Hands Won: " + opponentHandsWon);
		System.out.println();
		System.out.print("Discard Pile: ");
		if(hasDiscard)
			prettyPrintCard(discardPileTop);
		else
			System.out.println("No Discard Card");
		System.out.println("My Hand: ");
		
		for(int i = 0; i < myHand.size(); i++)
		{
			prettyPrintCard(myHand.get(i));
		}
	}
	
	public int[] peekStock()
	{
		return stockPile.peek();
	}
	
	/** Returns playerNumber's hand. */
	public ArrayList<int[]> getMyHand(int playerNumber)
	{
		if (playerNumber == 1) {
			return playerOneHand;
		} else {
			return playerTwoHand;
		}
	}
	
	/** Returns playerNumber's score. */
	public int getPlayerScore(int playerNumber)
	{
		if (playerNumber == 1) {
			return playerOneScore;
		} else {
			return playerTwoScore;
		}
	}
	
	/** Returns playerNumber's hands won. */
	public int getHandsWon(int playerNumber) 
	{
		if (playerNumber == 1) {
			return playerOneHandsWon;
		} else {
			return playerTwoHandsWon;
		}
	}
	
	/** Returns top card of discard pile. */
	public int[] getDiscardPileTop()
	{
		return discardPileTop;
	}
	
	/** Removes top card of stock pile and returns it. */
	public int[] getStockPileTop()
	{
		if(stockPile.size()<2)
		{
			//Creates a new stock pile if the current one has run out
			Stack<int[]> deck = Helper.createDeck();
		
			deck.removeAll(playerOneHand);
			deck.removeAll(playerTwoHand);
			deck.remove(discardPileTop);
			
			if(stockPile.empty())
			{
				setStockPile(deck);
				return stockPile.pop();
			}
			else
			{
				int[] lastCard = stockPile.pop();
				deck.remove(lastCard);
				setStockPile(deck);
				return lastCard;
			}
		}
		else
			return stockPile.pop();
	}
	
	/** Sets playerNumber's hand. */
	public void setMyHand(int playerNumber, ArrayList<int[]> hand)
	{
		if (playerNumber == 1) {
			playerOneHand = hand;
		} else {
			playerTwoHand = hand;
		}
	}
	
	/** Sets the stock pile. */
	public void setStockPile(Stack<int[]> stockPile)
	{
		this.stockPile = stockPile;
	}
	
	/** Adds card to the top of the discard pile. */
	public void discardCard(int[] card)
	{
		hasDiscard = true;
		discardPileTop = card;
	}
	
	/** Adds newPoints to playerNumber's score. */
	public void updateMyScore(int playerNumber, int newPoints)
	{
		if (playerNumber == 1) {
			playerOneScore += newPoints;
		} else {
			playerTwoScore += newPoints;
		}
	}
	
	/** Adds one to playerNumber's hands won. */
	public void updateHandsWon(int playerNumber)
	{
		if (playerNumber == 1) {
			playerOneHandsWon++;
		} else {
			playerTwoHandsWon++;
		}
	}
	
	/* Returns the current player's turn. */
	public int getTurn() {
		return player_turn;
	}
	
	/* Changes the turn to the next player. */
	public void nextTurn() {
		if (player_turn == 1) {
			player_turn = 2;
		} else {
			player_turn = 1;
		}
	}
	
	public void endRound()
	{
		System.out.println("player number: " + player_turn);
		ArrayList<int[]> hand = getMyHand(player_turn);
		// Compute score
		int player_deadwood = Helper.getDeadwood(hand);//Helper.deadWoodPoints(hand);
		int opponent_deadwood;
		if (player_turn == 1) opponent_deadwood = Helper.getDeadwood(playerTwoHand);//Helper.deadWoodPoints(playerTwoHand);
		else opponent_deadwood = Helper.getDeadwood(playerOneHand);//Helper.deadWoodPoints(playerOneHand);
		
		int player_score = 0;
		int opponent_score = 0;
		int player_hands = 0;
		int opponent_hands = 0;
		//System.out.println("Turn: " + player_turn);
		System.out.println("Player: " + player_deadwood);
		System.out.println("Opponent: " + opponent_deadwood);
		if (player_deadwood == 0) {
			System.out.println("Gin");
			// Player has gone gin, so award 25 point bonus
			player_score = 25 + opponent_deadwood;
			player_hands = 1;
		} else if (player_deadwood < opponent_deadwood) {
			System.out.println("Knock");
			// Successful knock, player gets points
			player_score = opponent_deadwood - player_deadwood;
			player_hands = 1;
		} else {
			// Player has been undercut, so opponent gets 25 point bonus
			System.out.println("Undercut");
			opponent_score = 25 + (player_deadwood - opponent_deadwood);
			opponent_hands = 1;
		}
		
		if (player_turn == 1) {
			playerOneScore += player_score;
			playerOneHandsWon += player_hands;
			playerTwoScore += opponent_score;
			playerTwoHandsWon += opponent_hands;
		} else {
			playerTwoScore += player_score;
			playerTwoHandsWon += player_hands;
			playerOneScore += opponent_score;
			playerOneHandsWon += opponent_hands;
		}
		roundEnd = true;
	}
	
	public void removeDiscard()
	{
		hasDiscard = false;
		discardPileTop = null;
	}
	
	public void setTurn(int turn)
	{
		player_turn = turn;
	}
}
