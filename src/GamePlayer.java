
public abstract class GamePlayer {
	public int player_num; // The number for this player
	protected GamePlayerGUI playerGUI;
	
	/** Constructor: creates new GamePlayer object. 
	 * Parameter num: the number for this player.
	 */
	protected GamePlayer(int num) {
		player_num = num;
	}
	
	/** Make a move. In one move, the player draws a card from the deck, 
	 * discards a card from its current hand, and decides whether to knock. 
	 * The player returns a state object with each of these changes.
	 * 
	 * Parameter state: a GameState object representing the current state of
	 * the game.
	 */
	public abstract GameState makeMove(GameState state);
	
	public abstract void updateGUI(GameState state);
	
	public abstract void incrementRoundNumber();
}
