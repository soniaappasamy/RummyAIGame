import java.util.ArrayList;

public class GameTreeNode {
	
	private GameState nodeState;
	private ArrayList<GameTreeNode> successorNodes;
	private int value; // Not set in constructor because set by minimax or stateEval
	private GameTreeNode best_successor;
	public boolean discard;
	public int[] discardedCard;
	
	public GameTreeNode(GameState state, boolean isDiscard, int[] dropCard)
	{
		nodeState = state;
		successorNodes = new ArrayList<GameTreeNode>();
		discard = isDiscard;
		discardedCard = dropCard;
	}
	
	/** Updates the state of this node */
	public void updateNodeState(GameState newState)
	{
		nodeState = newState;
	}
	
	/** Updates the value of this node */
	public void updateNodeValue(int newValue)
	{
		value = newValue;
	}
	
	/** Set the best successor of this node. */
	public void setBestSuccessor(GameTreeNode best_successor) {
		this.best_successor = best_successor;
	}
	
	/** Adds this node to the list of successors */
	public void addSuccessorNode(GameTreeNode newSuccessorNode)
	{
		successorNodes.add(newSuccessorNode);
	}
	
	/** Returns the state of this node */
	public GameState getState()
	{
		return nodeState;
	}
	
	/** Returns the successor nodes of this node */
	public ArrayList<GameTreeNode> getSuccessorNodes()
	{
		return successorNodes;
	}
	
	
	public int getValue()
	{
		return value;
	}
	
	public GameTreeNode getBestSuccessor() {
		return best_successor;
	}
	
}
