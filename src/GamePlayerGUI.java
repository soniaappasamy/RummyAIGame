import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

public class GamePlayerGUI extends JFrame {
	
	private int playerNum;
	private int roundNumber;
	
	private ArrayList<JButton> cards;
	private boolean cardSelected;
	private int selectedCard;
	
	private JButton discardCard;
	private JButton stockCard;
	private boolean pileSelected;
	private int selectedPile;
	
	private JButton knockButton;
	private JButton ginButton;
	private boolean endSelected;
	private int selectedEnd;
	
	private JTextArea scoreBoard;
	private JTextArea playerConsole;
	
	public GamePlayerGUI(String title, int playernum)
	{
		this.setTitle(title);
		this.setSize(800, 300);
		roundNumber = 0;
		
		JPanel mainPanel = new JPanel();
		FlowLayout grid = new FlowLayout();
		//BoxLayout grid = new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS);
		//GridLayout grid = new GridLayout(2,2);
		mainPanel.setLayout(grid);
		
		JPanel handPane = new JPanel();
		cards = new ArrayList<JButton>();
		for(int i = 0; i < 11; i++)
		{
			JButton temp = new JButton();
			temp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cardSelected = true;
					Object source = e.getSource();
					for(int i = 0; i < cards.size(); i++)
					{
						if(source == cards.get(i))
							selectedCard = i+1;
					}
				}
			});
			temp.setSize(50, 50);
			//temp.setLocation(50*i, 0);
			temp.setEnabled(false);
			cards.add(temp);
			handPane.add(cards.get(i));
		}
		cards.get(10).setVisible(false);
		mainPanel.add(handPane);
		
		JPanel pilePanel = new JPanel();
		discardCard = new JButton("Discard");
		//discardCard.setLocation(200, 0);
		discardCard.setEnabled(false);
		discardCard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pileSelected = true;
				selectedPile = 0;
			}
		});
		pilePanel.add(discardCard);
		
		stockCard = new JButton("Stock");
		//stockCard.setLocation(200, 100);
		stockCard.setEnabled(false);
		stockCard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pileSelected = true;
				selectedPile = 1;
			}
		});
		pilePanel.add(stockCard);
		mainPanel.add(pilePanel);
		
		ginButton = new JButton("Go Gin");
		ginButton.setVisible(false);
		ginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				endSelected = true;
				selectedEnd = 0;
			}
		});
		pilePanel.add(ginButton);
		
		knockButton = new JButton("Knock");
		knockButton.setVisible(false);
		knockButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				endSelected = true;
				selectedEnd = 1;
			}
		});
		pilePanel.add(knockButton);
		
		JPanel scoreBoardPane = new JPanel();
		scoreBoard = new JTextArea("score board");
		scoreBoard.setEditable(false);
		scoreBoardPane.add(scoreBoard);
		mainPanel.add(scoreBoardPane);
		
		JPanel consolePanel = new JPanel();
		playerConsole = new JTextArea();
		playerConsole.setEditable(false);
		consolePanel.add(playerConsole);
		mainPanel.add(consolePanel);
		
		this.add(mainPanel);
		this.setVisible(true);
		
		playerNum = playernum;
	}
	
	public void incrementRound()
	{
		roundNumber++;
	}
	
	public void prepHumanGUI()
	{
		discardCard.setEnabled(true);
		stockCard.setEnabled(true);
		
		for(int i = 0; i < 11; i++)
		{
			cards.get(i).setEnabled(true);
		}
	}
	
	public GameState promptHumanMove(GameState state)
	{
		playerConsole.setText("Click on the discard pile or the stock pile to pick up a card");
		
		int[] pickedUpCard;
		pileSelected = false;
		while(!pileSelected) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if(selectedPile == 0) //Picking up from the discard pile
		{
			discardCard.setIcon(null);
			pickedUpCard = state.getDiscardPileTop();
		}
		else //Picking up from the stock pile
		{
			pickedUpCard = state.getStockPileTop();
			String temp = state.imageFormatCard(state.peekStock());
			ImageIcon newImg = new ImageIcon(new ImageIcon("cards/" + temp + ".png").getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
			//stockCard.setIcon(newImg);
		}
		
		// get user console input on their desired discard
		ArrayList<int[]> previousHand = state.getMyHand(playerNum);
		cards.get(10).setVisible(true);
		String temp = state.imageFormatCard(pickedUpCard);
		ImageIcon newImg = new ImageIcon(new ImageIcon("cards/" + temp + ".png").getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
		cards.get(10).setIcon(newImg);
		
		ginButton.setVisible(true);
		knockButton.setVisible(true);
		endSelected = false;
		
		playerConsole.setText("Click on the card you want to discard\nIf ending, choose \"Knock\" or \"Go Gin\" first");
		
		cardSelected = false;
		while(!cardSelected) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		cards.get(10).setVisible(false);
		ginButton.setVisible(false);
		knockButton.setVisible(false);
		
		if(selectedCard == (previousHand.size()+1))
		{
			state.discardCard(pickedUpCard);
		}
		else
		{
			state.discardCard(previousHand.get(selectedCard-1));
			previousHand.remove(selectedCard-1);
			previousHand.add(pickedUpCard);
			state.setMyHand(playerNum, previousHand);
		}
		
		if(endSelected)
		{
			state.endRound();
		}
		
		playerConsole.setText("");
		
		return state;
	}
	
	public void displayAIMove(String move)
	{
		playerConsole.setText("Last Move:\n" + move);
	}
	
	public void updateGUI(GameState state)
	{
		ArrayList<int[]> myHand = state.getMyHand(playerNum);
		
		for(int i = 0; i < myHand.size(); i++)
		{
			String temp = state.imageFormatCard(myHand.get(i));
			ImageIcon newImg = new ImageIcon(new ImageIcon("cards/" + temp + ".png").getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
			cards.get(i).setIcon(newImg);
			cards.get(i).setDisabledIcon(newImg);
		}
		
		if(state.hasDiscard)
		{
			String temp = state.imageFormatCard(state.getDiscardPileTop());
			ImageIcon newImg = new ImageIcon(new ImageIcon("cards/" + temp + ".png").getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
			discardCard.setIcon(newImg);
			discardCard.setDisabledIcon(newImg);
		}
		
		String temp = state.imageFormatCard(state.peekStock());
		ImageIcon newImg = new ImageIcon(new ImageIcon("cards/" + temp + ".png").getImage().getScaledInstance(50, 75, Image.SCALE_SMOOTH));
		//stockCard.setIcon(newImg);
		//stockCard.setDisabledIcon(newImg);
		
		scoreBoard.setText(state.prettyFormatScoreBoard(playerNum) + "\nRound: " + roundNumber);
	}

}
