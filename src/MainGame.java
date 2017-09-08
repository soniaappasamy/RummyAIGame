import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.Stack;

public class MainGame {

	private GamePlayer playerOne;
	private GamePlayer playerTwo;
	private GameState state;
	private static final int HEARTS = 0;
	private static final int DIAMONDS = 1;
	private static final int CLUBS = 2;
	private static final int SPADES = 3;
	
	
	/** Creates a new deck of 52 cards, shuffles the deck randomly, deals 10 
	 * cards to each player, and updates the state. */
	public void dealCards() {
		Stack<int[]> deck = Helper.createDeck();
		
		// Deal cards from top of deck sequentially to each hand
		ArrayList<int[]> playerOneHand = new ArrayList<int[]>();
		ArrayList<int[]> playerTwoHand = new ArrayList<int[]>();
		for (int i = 1; i <= 20; i++) {
			if (i % 2 == 1) {
				// Deal to player 1
				playerOneHand.add(deck.pop());
			} else {
				// Deal to player 2
				playerTwoHand.add(deck.pop());
			}
		}
		
		// Update state
		state.setMyHand(1, playerOneHand);
		state.setMyHand(2, playerTwoHand);
		state.setStockPile(deck);
	}
	
	/** Randomly selects one of the two players to be considered the dealer
	 *  (dealer gets to move first) */
	public GamePlayer chooseDealer()
	{
		int playerNum = (new Random()).nextInt(2);
		
		if (playerNum == 0)
			return playerOne;
		else
			return playerTwo;
	}
	
	/** Determines who's turn it is next based on who just made a move */
	public GamePlayer getNextPlayer(GamePlayer currentPlayer)
	{
		if(currentPlayer == playerOne)
			return playerTwo;
		else
			return playerOne;
	}
	
	/** The primary function that plays the game until one player wins. */
	public void play()
	{
		playerOne = new HumanPlayer(1);
		playerTwo = new AIPlayer(2);
		
		GamePlayer currentPlayer = chooseDealer();
		state = new GameState(0, 0, 0, 0, currentPlayer.player_num);
		
		while (state.getPlayerScore(1) < 100 && state.getPlayerScore(2) < 100) {
			// Rounds continue until one player scores 100 or more
			dealCards();
			state.roundEnd = false;
			state.removeDiscard();
			playerOne.incrementRoundNumber();
			playerTwo.incrementRoundNumber();
			((AIPlayer) playerTwo).reset(state);
			
			playerOne.updateGUI(state);
			playerTwo.updateGUI(state);
			
			while (!state.roundEnd) {
				// Players alternate moves until round is over
				state = currentPlayer.makeMove(state);
				state.nextTurn();
				currentPlayer = getNextPlayer(currentPlayer);
			}
			currentPlayer = chooseDealer();
			state.setTurn(currentPlayer.player_num);
		}
		
		// Compute final scores with bonuses (100 points for reaching 100 and 20 
		// for each hand won)
		if (state.getPlayerScore(1) >= 100) {
			state.updateMyScore(1, 100 + state.getHandsWon(1) * 20);
			state.updateMyScore(2, state.getHandsWon(2) * 20);
		} else {
			state.updateMyScore(2, 100 + state.getHandsWon(2) * 20);
			state.updateMyScore(1, state.getHandsWon(1) * 20);
		}
		
		playerOne.updateGUI(state);
		playerTwo.updateGUI(state);
	}
	
	public static void main(String[] args)
	{	
		// Calls play()
		MainGame game = new MainGame();
		game.play();
	}
	
}
