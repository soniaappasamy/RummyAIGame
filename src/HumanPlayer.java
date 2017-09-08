import java.util.ArrayList;
import java.util.Scanner;

public class HumanPlayer extends GamePlayer {
	
	/** Constructor: creates new HumanPlayer object. 
	 * Parameter num: the number for this player.
	 */
	public HumanPlayer(int num) {
		super(num);
		playerGUI = new GamePlayerGUI("Human Player", player_num);
		playerGUI.setLocation(0, 0);
		playerGUI.prepHumanGUI();
	}
	
	public GameState makeMove(GameState state) {
		
		playerGUI.updateGUI(state);
		state = playerGUI.promptHumanMove(state);
		playerGUI.updateGUI(state);
		
		return state;
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
