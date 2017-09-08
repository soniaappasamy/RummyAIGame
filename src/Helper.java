import java.util.Collections;
import java.util.ArrayList;
import java.util.Stack;

public class Helper {
	/* This class stores helper functions needed in the other
	 * classes. The constructor cannot be used. */
	
	private static final int HEARTS = 0;
	private static final int DIAMONDS = 1;
	private static final int CLUBS = 2;
	private static final int SPADES = 3;
	
	private Helper() {}
	
	/* Returns a shuffled deck of 52 cards. */
	public static Stack<int[]> createDeck() {
		// Populate deck with 4 suits, 13 cards each [RANK, SUIT]
		Stack<int[]> deck = new Stack<int[]>();
		for (int j = HEARTS; j <= SPADES; j++) {
			for (int i = 1; i <= 13; i++) {
				int[] card = new int[] {i, j};
				deck.push(card);
			}
		}
		Collections.shuffle(deck);
		return deck;
	}
	
	public static ArrayList<ArrayList<int[]>> getDeadwoodPossibilities(int[] card, ArrayList<int[]> hand)
	{
		ArrayList<ArrayList<int[]>> possibilities = new ArrayList<ArrayList<int[]>>();
		
		int rank = card[0];
		int suit = card[1];
		
		boolean upOne = false;
		boolean upTwo = false;
		boolean downOne = false;
		boolean downTwo = false;
		
		// First, check if card is in run (there is at least a 3 card run)
		for(int i = 0; i < hand.size(); i++)
		{
			int[] temp = hand.get(i);
			
			if(temp != card)
			{
				if(temp[1] == suit)
				{
					if(temp[0] == rank+1)
						upOne = true;
					else if(temp[0] == rank+2)
						upTwo = true;
					else if(temp[0] == rank-1)
						downOne = true;
					else if(temp[0] == rank-2)
						downTwo = true;
				}
			}
		}
		if(upOne && upTwo)
		{
			ArrayList<int[]> temp = new ArrayList<int[]>();
			temp.add(new int[]{rank,suit});
			temp.add(new int[]{rank+1,suit});
			temp.add(new int[]{rank+2,suit});
			possibilities.add(temp);
		}
		else if(upOne && downOne)
		{
			ArrayList<int[]> temp = new ArrayList<int[]>();
			temp.add(new int[]{rank-1,suit});
			temp.add(new int[]{rank,suit});
			temp.add(new int[]{rank+1,suit});
			possibilities.add(temp);
		}
		else if(downOne && downTwo)
		{
			ArrayList<int[]> temp = new ArrayList<int[]>();
			temp.add(new int[]{rank-2,suit});
			temp.add(new int[]{rank-1,suit});
			temp.add(new int[]{rank,suit});
			possibilities.add(temp);
		}
		
		// Next, check if card is in set (there is at least a 3 card set)
		int same_rank = 0;
		ArrayList<int[]> sameRank = new ArrayList<int[]>();
		sameRank.add(new int[]{rank,suit});
		for (int i = 0; i < 10; i++) {
			if (hand.get(i)[0] == rank && hand.get(i)[1] != suit) {
				same_rank++;
				sameRank.add(new int[]{hand.get(i)[0],hand.get(i)[1]});
			}
		}
		if(same_rank >= 2)
		{
			possibilities.add(sameRank);
		}
		
		return possibilities;
	}
	
	// dynamic programming approach
	public static int remaining(ArrayList<ArrayList<int[]>> dups, ArrayList<int[]> takenVals, ArrayList<int[]> orig)
	{
		if(dups.size() == 0)
		{
			for(int i = 0; i < takenVals.size(); i++)
			{
				orig.remove(takenVals.get(i));
			}
			
			int deadwoodCount = 0;
			for(int i = 0; i < orig.size(); i++)
			{
				deadwoodCount += Math.min(10, orig.get(i)[0]);
			}
			
			return deadwoodCount;
		}
		
		ArrayList<int[]> current = dups.get(0);
		ArrayList<int[]> newCurrent = new ArrayList<int[]>();
		for(int i = 0; i < current.size(); i++)
		{
			if(!takenVals.contains(current.get(i)))
				newCurrent.add(current.get(i));
		}
		
		if(newCurrent.size() < 3)
		{
			dups.remove(0);
			return remaining(dups, takenVals, orig);
		}
		else
		{
			dups.remove(0);
			ArrayList<int[]> newTaken = (ArrayList<int[]>) takenVals.clone();
			newTaken.addAll(newCurrent);
			return Math.min(remaining(dups,newTaken,orig), remaining(dups,takenVals,orig));
		}
	}
	
	public static int getDeadwood(ArrayList<int[]> hand)
	{
		int deadwood = 0;
		ArrayList<ArrayList<int[]>> possibilities = new ArrayList<ArrayList<int[]>>();
		for (int i = 0; i < 10; i++) {
			// Check if each card in player's hand is deadwood and add points
			int[] card = hand.get(i);
			ArrayList<ArrayList<int[]>> temp = Helper.getDeadwoodPossibilities(card, hand);
			if (temp.size() == 0) {
				// Add points (10 for face cards, otherwise rank)
				deadwood += Math.min(10, card[0]);
			}
			//only add new ones to the possibilities list
			else
			{
				possibilities.addAll(temp);
			}
		}
		
		//also combine ones that are in the same run, but longer
		//might not actually have to do this
		int index = 0;
		while(index < possibilities.size())
		{
			ArrayList<int[]> curr = possibilities.get(index);
			
			int j = index+1;
			while(j < possibilities.size())
			{
				ArrayList<int[]> comp = possibilities.get(j);
				if(curr.get(0)[1] != curr.get(0)[1]) //The case that it is a run
				{
					if(comp.get(0)[1] != comp.get(0)[1] && (comp.get(0)[0] == curr.get(0)[0])) //The same run
					{
						possibilities.remove(index);
					}
					else
						index++;
				}
				else //combine the overlaps
				{
					ArrayList<int[]> combined = new ArrayList<int[]>();
					boolean hadOverlap = false;
						
					for(int x = 0; x < 3; x++)
					{
						boolean dup = false;
						for(int y = 0; y < 3; y++)
						{
							//ones that are overlapping
							if(curr.get(x)[0] == comp.get(y)[0] && curr.get(x)[1] == comp.get(y)[0])
							{
								dup = true;
								hadOverlap = true;
							}
						}
						if(!dup)
							combined.add(curr.get(x));
					}
					combined.addAll(comp);
						
					if(hadOverlap)
					{
						possibilities.remove(j);
						possibilities.set(index, combined);
					}
					index++;
				}
			}
		}
		
		ArrayList<int[]> orig = new ArrayList<int[]>();
		for(int i = 0; i < possibilities.size(); i++)
		{
			orig.addAll(possibilities.get(i));
		}
		
		return deadwood + remaining(possibilities, new ArrayList<int[]>(), orig);
	}
	
	/** Checks if a card is deadwood/unmatched.
	 * Parameter card: the given card, hand: the player's hand.
	 */
	public static boolean isDeadwood(int[] card, ArrayList<int[]> hand) {
		int rank = card[0];
		int suit = card[1];
		
		boolean upOne = false;
		boolean upTwo = false;
		boolean downOne = false;
		boolean downTwo = false;
		
		// First, check if card is in run (there is at least a 3 card run)
		for(int i = 0; i < hand.size(); i++)
		{
			int[] temp = hand.get(i);
			
			if(temp != card)
			{
				if(temp[1] == suit)
				{
					if(temp[0] == rank+1)
						upOne = true;
					else if(temp[0] == rank+2)
						upTwo = true;
					else if(temp[0] == rank-1)
						downOne = true;
					else if(temp[0] == rank-2)
						downTwo = true;
				}
			}
		}
		
		if(upOne && upTwo)
			return false;
		else if(upOne && downOne)
			return false;
		else if(downOne && downTwo)
			return false;
	
		// Next, check if card is in set (there is at least a 3 card set)
		int same_rank = 0;
		for (int i = 0; i < 10; i++) {
			if (hand.get(i)[0] == rank && hand.get(i)[1] != suit) {
				same_rank++;
			}
			if (same_rank >= 2) {
				return false;
			}
		}
		return true;
	}
	
	/* Returns the total number of deadwood points in the hand. */
	public static int deadWoodPoints(ArrayList<int[]> hand) {
		int deadwood = 0;
		for (int i = 0; i < 10; i++) {
			// Check if each card in player's hand is deadwood and add points
			int[] card = hand.get(i);
			if (Helper.isDeadwood(card, hand)) {
				// Add points (10 for face cards, otherwise rank)
				deadwood += Math.min(10, card[0]);
			}
		}
		return deadwood;
	}
}
