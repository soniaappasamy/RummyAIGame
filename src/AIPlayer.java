import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Stack;

public class AIPlayer extends GamePlayer {
	GameTreeNode game_tree; // The game tree for the current state
	int depth_cutoff; // The depth cutoff for minimax
	private Stack<int[]> poss_cards;
	private Stack<Double> weights;
	private ArrayList<int[]> opponent_hand;
	private ArrayList<int[]> discard_history;
	private static final int HEARTS = 0;
	private static final int DIAMONDS = 1;
	private static final int CLUBS = 2;
	private static final int SPADES = 3;
	
	/** Constructor: creates new AIPlayer object. 
	 * Parameter num: the number for this player.
	 */
	public AIPlayer(int num) {
		super(num);
		opponent_hand = new ArrayList<int[]>();
		discard_history = new ArrayList<int[]>();
		playerGUI = new GamePlayerGUI("AI Player", player_num);
		playerGUI.setLocation(0, 350);
		depth_cutoff = 4; // Must be even number
	}
	
	public GameState makeMove(GameState state) {
		
		playerGUI.updateGUI(state);
		
		if (state.hasDiscard) {
			discard_history.add(state.getDiscardPileTop());
		}
		
		// Estimate opponent hand by choosing from remaining cards
		predictHand(state);
//		for (int i = 0; i < 10; i++) {
//			System.out.println(opponent_hand.get(i)[0]);
//			System.out.println(opponent_hand.get(i)[1]);
//		}
		// Create new deck for game tree
		Stack<int[]> deck = Helper.createDeck();
		deck.removeAll(state.getMyHand(player_num));
		deck.removeAll(opponent_hand);
		deck.removeAll(discard_history);
		
		// Initialize game_tree using a predicted state
		GameState prediction_state = new GameState(state, deck, opponent_hand);
		game_tree = new GameTreeNode(prediction_state, false, new int[]{0,0});
		generateTree(game_tree, 0);
		
		// Run minimax on game_tree, which sets best_successor
		minimax(game_tree, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
		String move;
		GameTreeNode chosen = game_tree.getBestSuccessor();
		
		if (chosen.getState().roundEnd) {
			// Player is knocking or going gin
			move = "Knocked";
			playerGUI.displayAIMove(move);
			playerGUI.updateGUI(state);
			return new GameState(state, chosen.discard);
		}
		
		int[] pickUp;
		if(chosen.discard)
		{
			pickUp = state.getDiscardPileTop();
			move = "Picked up: " + state.prettyFormatCard(pickUp) + " from discard";
		}
		else
		{
			pickUp = state.getStockPileTop();
			move = "Picked up: " + state.prettyFormatCard(pickUp) + " from stock";
		}
		
		if(chosen.discardedCard[0] == 0 && chosen.discardedCard[1] == 0)
		{
			move = move + "\nDiscarded: " + state.prettyFormatCard(pickUp);
			state.discardCard(pickUp);
		}
		else
		{
			ArrayList<int[]> currHand = state.getMyHand(player_num);
			currHand.remove(chosen.discardedCard);
			currHand.add(pickUp);
			state.setMyHand(player_num, currHand);
			state.discardCard(chosen.discardedCard);
			move = move + "\nDiscarded: " + state.prettyFormatCard(chosen.discardedCard);
		}
		playerGUI.displayAIMove(move);
		playerGUI.updateGUI(state);
		return state;
	}
	
	/** Generates a game tree down to a specified depth limit. 
	 * Parameters: node is the current node of the tree and depth is the depth of 
	 * node in the overall tree. */
	private void generateTree(GameTreeNode node, int depth) {

		// Debug: print player 2's (AI player) hand at start of function
		/*for(int i = 0; i < 10; i++)
		{
			System.out.print(node.getState().getMyHand(2).get(i)[0] + " ");
		}
		System.out.println("");*/
		
		if (depth == depth_cutoff) {
			// Reached the depth cutoff, so use stateEval
			node.updateNodeValue(stateEval(node.getState()));
			return;
		}

		GameState current_state = node.getState();
		ArrayList<int[]> hand = current_state.getMyHand(current_state.getTurn());
		
		if(current_state.hasDiscard)
		{
			for (int i = 0; i < 10; i++) {
				// New successor for discarding each card in hand, draw from discard
				GameState next = new GameState(current_state, true, hand.get(i));
				GameTreeNode successor = new GameTreeNode(next, true, hand.get(i));
				/*
				for(int j = 0; j < 10; j++)
				{
					System.out.print(successor.getState().getMyHand(player_num).get(j)[0] + " ");
				}
				System.out.println("");
				*/
				node.addSuccessorNode(successor);
			}
		}
		
		//System.out.println(hand.size());
		for (int i = 0; i < 10; i++) {
			// New successor for discarding each card in hand, draw from stock pile
			GameState next = new GameState(current_state, false, hand.get(i));
			GameTreeNode successor = new GameTreeNode(next, false, hand.get(i));
			node.addSuccessorNode(successor);
		}
		
		if (current_state.hasDiscard) {
			// Another state for discarding same card as drawn - discard
			GameState next = new GameState(current_state, true, new int[]{0,0});
			GameTreeNode successor = new GameTreeNode(next,true, new int[]{0,0});
			node.addSuccessorNode(successor);
		}
		
		// Another state for discarding same card as drawn - stock
		GameState next2 = new GameState(current_state, false, new int[]{0,0});
		GameTreeNode successor2 = new GameTreeNode(next2,false, new int[]{0,0});
		node.addSuccessorNode(successor2);
		
		// Debug: print all successor hands of player 2 (AI player)
		/*for (int i = 0; i < node.getSuccessorNodes().size(); i++) {
			System.out.println("Successor " + i + ":");
			hand = node.getSuccessorNodes().get(i).getState().getMyHand(2);
			for(int j = 0; j < 10; j++)
			{
				System.out.print(hand.get(j)[0] + " ");
			}
			System.out.println("");
		}*/
		
		// Recursively generate tree
		for (int i = 0; i < node.getSuccessorNodes().size(); i++) {
			generateTree(node.getSuccessorNodes().get(i), depth + 1);
		}
		
		// Can only knock if at most 10 points of deadwood
		if (Helper.deadWoodPoints(hand) <= 10) {
			// Create two new states for knocking or going gin, and update values
			GameState next = new GameState(current_state, true);
			GameTreeNode successor = new GameTreeNode(next, true, next.getDiscardPileTop());
			node.addSuccessorNode(successor);
			successor.updateNodeValue(stateEval(next));
			
			next = new GameState(current_state, false);
			successor = new GameTreeNode(next, false, next.getDiscardPileTop());
			node.addSuccessorNode(successor);
			successor.updateNodeValue(stateEval(next));
		}
	}
	
	/** The evaluation function that returns a value for the given state.
	 * Currently, the function is the difference between the sum of the point 
	 * values of the unmatched "deadwood" cards in the player's hand, and the sum 
	 * of the deadwood in the opponent's hand. Higher values are better.
	 * 
	 * Parameter state: the state to be evaluated.
	 */
	private int stateEval(GameState state) {
		//System.out.println("STATE: " + state.toString());
		int player_deadwood = Helper.deadWoodPoints(state.getMyHand(player_num));
		int opponent_deadwood;
		if (player_num == 1) opponent_deadwood = Helper.deadWoodPoints(state.getMyHand(2));
		else opponent_deadwood = Helper.deadWoodPoints(state.getMyHand(1));
		
		return opponent_deadwood - player_deadwood;
	}
	
	
	/** The minimizing function for the recursive minimax algorithm. 
	 * 
	 * Parameters: curr_node is the current node, depth is the tree depth of 
	 * curr_node, and a and b are alpha and beta for alpha-beta pruning. */
	private int minimax(GameTreeNode curr_node, int depth, int a, int b) {
		ArrayList<GameTreeNode> successors = curr_node.getSuccessorNodes();
		if (successors.isEmpty()) {
			// Leaf node or reached max depth
			return curr_node.getValue();
		}
		
		GameTreeNode best_successor = successors.get(0);
		int val = Integer.MIN_VALUE;
		
		for (int i = 0; i < successors.size(); i++) {
			int new_val = maximin(successors.get(i), depth + 1, a, b);
			//System.out.println("new succ val: " + new_val + " old val: " + val);
			if (new_val > val) {
				// Found a better successor
				val = new_val;
				best_successor = successors.get(i);
			}
			if (val >= b) {
				// Value is better than best case beta
				break;
			}
			// Update worst case alpha
			a = Math.max(a, val);
		}
		curr_node.updateNodeValue(val);
		curr_node.setBestSuccessor(best_successor);
		return val;
	}
	
	/** The maximizing function for the recursive minimax algorithm. 
	 * 
	 * Parameters: curr_node is the current node, depth is the tree depth of 
	 * curr_node, and a and b are alpha and beta for alpha-beta pruning. */
	private int maximin(GameTreeNode curr_node, int depth, int a, int b) {
		ArrayList<GameTreeNode> successors = curr_node.getSuccessorNodes();
		if (successors.isEmpty()) {
			// Leaf node or reached max depth
			return curr_node.getValue();
		}
		
		GameTreeNode best_successor = successors.get(0);
		int val = Integer.MAX_VALUE;
		for (int i = 0; i < successors.size(); i++) {
			int new_val = minimax(successors.get(i), depth + 1, a, b);
			if (new_val < val) {
				// Found a better successor
				val = new_val;
				best_successor = successors.get(i);
			}
			if (val <= a) {
				// Value is worse than worst case alpha
				break;
			}
			// Update best case beta
			b = Math.min(b, val);
		}
		curr_node.updateNodeValue(val);
		curr_node.setBestSuccessor(best_successor);
		return val;
	}
	
	private void predictHand(GameState state) {
		if (state.hasDiscard) {
			int i = poss_cards.indexOf(state.getDiscardPileTop());
			if (i != -1) {
				poss_cards.remove(i);
				weights.remove(i);
			}
			
			int rank = state.getDiscardPileTop()[0];
			int suit = state.getDiscardPileTop()[1];
			for (int j = 0; j < poss_cards.size(); j++) {
				int[] card = poss_cards.get(j);
				if (card[0] == rank) {
					weights.set(j, weights.get(j) * 0.5);
				} else if (card[1] == suit) {
					weights.set(j, weights.get(j) * (double) Math.abs(rank - card[0]) / 12);
				}
			}
			
			opponent_hand.clear();
			Stack<Double> weights_copy = new Stack<Double>();
			weights_copy.addAll(weights);
			weights_copy.sort(Comparator.reverseOrder());
			double[] highest_weights = new double[10];
			for (int j = 0; j < 10; j++) {
				highest_weights[j] = weights_copy.pop();
			}
			
			Stack<int[]> poss_copy = new Stack<int[]>();
			poss_copy.addAll(poss_cards);
			weights_copy.clear();
			weights_copy.addAll(weights);
			// Get five highest weighted cards
			for (int j = 0; j < 5; j++) {
				int index = weights_copy.indexOf(highest_weights[j]);
				weights_copy.remove(index);
				opponent_hand.add(poss_copy.remove(index));
			}
			for (int j = 0; j < 5; j++) {
				opponent_hand.add(poss_copy.pop());
			}
		} else {
			Stack<int[]> poss_copy = new Stack<int[]>();
			poss_copy.addAll(poss_cards);
			for (int j = 0; j < 10; j++) {
				opponent_hand.add(poss_copy.pop());
			}
		}
	}
	
	public void reset(GameState state) {
		poss_cards = Helper.createDeck();
		poss_cards.removeAll(state.getMyHand(player_num));
		weights = new Stack<Double>();
		for (int i = 0; i < poss_cards.size(); i++) {
			weights.add(1.0);
		}
	}
	
	public void updateGUI(GameState state)
	{
		playerGUI.updateGUI(state);
	}
	
	public void incrementRoundNumber()
	{
		playerGUI.incrementRound();
	}
}
