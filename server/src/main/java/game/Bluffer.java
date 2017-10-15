package game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import tokenizer.CommandMessage;

/**
 * The famous 'Bluffer' game, what could we do without it?
 */
public class Bluffer implements Game{
	private List<Round> rounds;
	private Round currentRound;
	private List<Player> players;
	private State state;
	private boolean gameFinished;
	private int remainingRounds;

	private final int NUMBER_OF_ROUNDS = 3;
	private final int POINTS_FOR_CORRECT_ANSWER = 10;
	private final int POINTS_FOR_FAKE_SUCCESS = 5;

	public enum State { recieveFakes, askChoice };

	public Bluffer(String path)
	{
		rounds = new ArrayList<Round>();
		// read the json file
		BufferedReader br = null;
		try { br = new BufferedReader(new FileReader(path)); } 
		catch (FileNotFoundException e) { try {throw new FileNotFoundException("There json file could not be found.");} catch (FileNotFoundException e1) {}}
		Gson gson = new Gson();
		QuestionHolder questionHolder = gson.fromJson(br, QuestionHolder.class);
		for (Question question : questionHolder.questions)
		{
			Round round = new Round(question);
			rounds.add(round);
		}

		remainingRounds = NUMBER_OF_ROUNDS;
		gameFinished = true;
		state = null;
	}

	public void removePlayer(Player playerToRemove) throws IOException
	{
		players.remove(playerToRemove);
		currentRound.removePlayer(playerToRemove);

		if(!gameFinished)
		{
			// move the game to the next phase - ask choices phase
			if(state == State.recieveFakes && currentRound.fakeAnswer.size() == players.size())
				synchronized (currentRound.fakeAnswer) {
					if(state == State.recieveFakes) {
						state = State.askChoice;
						currentRound.setChoices();

						for (Player player : players)
							player.sendMessage(new CommandMessage("ASKCHOISES", currentRound.getChoices()));
					}
				}
			// move the game to the next phase
			else if(state == State.askChoice && currentRound.chosenAnswers.size() == players.size())
				synchronized (currentRound.chosenAnswers) {
					if(state == State.askChoice)
						FinalizeRound();
				}
		}
	}

	@Override	
	public void startGame(List<Player> players) throws IOException
	{
		gameFinished = false;
		this.players = players;
		setNextRound();
	}

	/**
	 * Start the next round out of numberOfRounds given
	 * @throws IOException
	 */
	private void setNextRound() throws IOException
	{
		state = State.recieveFakes;
		// if there are another rounds to roll
		if(remainingRounds > 0)
		{
			currentRound = rounds.remove((int) (Math.random()*rounds.size()));
			for (Player player : players)
				player.sendMessage(new CommandMessage("ASKTXT", currentRound.questionText));

			remainingRounds--;
		}
		else
		{
			state = null;
			String finalScores = "";
			for (int i = 0; i < players.size(); i++) {
				finalScores += players.get(i).getNickName() + ": " + players.get(i).getScore() + "pts"; 
				if(i != players.size() - 1)
					finalScores += ", ";
			}

			for (Player player : players)
				player.sendMessage(new CommandMessage("GAMEMSG", "Summary: " + finalScores));

			gameFinished = true;
		}
	}

	/**
	 * The sender sent new TXTRESP message
	 */
	public void txtrespMessage(Player sender, CommandMessage response) throws IOException
	{
		if(!gameFinished)
		{
			if(state == State.recieveFakes)
			{
				currentRound.fakeAnswer.put(sender, response.getParams().toLowerCase());
				sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.ACCEPTED));
				// move the game to the next phase - ask choices phase
				if(currentRound.fakeAnswer.size() >= players.size())
					synchronized (currentRound.fakeAnswer) {
						if(state == State.recieveFakes)
						{
							state = State.askChoice;
							
							currentRound.setChoices();
							for (Player player : players)
								player.sendMessage(new CommandMessage("ASKCHOISES", currentRound.getChoices()));

						}
					}
			}
			else
				sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " game is currently not accepting fake answers."));
		}
		else
			sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " game has ended, need to start new game for that."));
	}

	/**
	 * The sender sent SELECTRESP message
	 */
	public void selectrespMessage(Player sender, CommandMessage response) throws IOException
	{
		if(!gameFinished)
		{
			if(state == State.askChoice)
			{
				try{
					if(currentRound.setChosenAnswer(sender, Integer.valueOf(response.getParams())))
						sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.ACCEPTED));
					else
						sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " please insert a number from given answers."));
				}
				catch(NumberFormatException e){
					sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " please insert a number."));
				}

				// move the game to the next phase
				if(currentRound.chosenAnswers.size() == players.size())
					synchronized (currentRound.chosenAnswers) {
						if(state == State.askChoice)
							FinalizeRound();
					}
			}
			else
				sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " game is currently not accepting answers."));
		}
		else
			sender.sendMessage(new CommandMessage("SYSMSG", response.getCommand() + " " + CommandMessage.Result.REJECTED + " game has ended, need to start new game for that."));
	}

	/**
	 * called when the round is about to be finished
	 * @throws IOException
	 */
	private void FinalizeRound() throws IOException
	{
		for (Player player : players) 
			player.sendMessage(new CommandMessage("GAMEMSG", "The correct answer is: " + currentRound.realAnswer));

		for (Player player : players)
		{
			// faked answers score.
			int pts = POINTS_FOR_FAKE_SUCCESS * currentRound.getNumberOfPlayersChooseYourAns(player);

			if(currentRound.isGotCorrectAnswer(player))
			{
				pts += POINTS_FOR_CORRECT_ANSWER;
				player.sendMessage(new CommandMessage("GAMEMSG", "correct! +" + pts + "pts"));
			}
			else
				player.sendMessage(new CommandMessage("GAMEMSG", "wrong! +" + pts + "pts"));

			player.setScore(player.getScore() + pts);
		}

		setNextRound();
	}

	public class QuestionHolder
	{
		@SerializedName("questions")
		private Question[] questions;


	}
	private class Question
	{
		@SerializedName("questionText")
		public String questionText;
		@SerializedName("realAnswer")
		public String realAnswer;
	}

	/**
	 * A class which represents single round.
	 */
	public class Round
	{
		public String questionText;
		public String realAnswer;
		public Map<Player, String> fakeAnswer;
		public List<String> askChoices;
		public Map<Player, Integer> chosenAnswers;

		public Round(Question q){
			fakeAnswer = new ConcurrentHashMap<Player, String>();
			askChoices = new ArrayList<String>();
			chosenAnswers = new ConcurrentHashMap<Player, Integer>();
			questionText = q.questionText;
			realAnswer = q.realAnswer.toLowerCase();
		}

		/**
		 * shuffle the given answers to the askChoices.
		 */
		public void setChoices()
		{
			List<String> list = new ArrayList<String>();

			list.add(realAnswer.toLowerCase());
			for (String fake : fakeAnswer.values())
				if(!list.contains(fake))
					list.add(fake);
			
			while(list.size() != 0)
				askChoices.add(list.remove((int) (Math.random() * list.size())));
		}

		/**
		 * get a string which list all choices.
		 * @return
		 */
		public String getChoices()
		{
			String ans = "";
			for (int i = 0; i < askChoices.size(); i++)
				ans += (i + "." + askChoices.get(i) + " ");
			return ans;
		}

		public void removePlayer(Player playerToRemove)
		{
			fakeAnswer.remove(playerToRemove);
			chosenAnswers.remove(playerToRemove);
		}

		/**
		 * Sets player chosen answer.
		 * @param player
		 * @param num The answer number.
		 * @return
		 */
		public boolean setChosenAnswer(Player player, Integer num)
		{
			if(num < 0 || askChoices.size() <= num)
				return false;
			else
			{
				chosenAnswers.put(player, num);
				return true;
			}
		}

		/**
		 * Returns the number of times other player has chosen playerFaked answer. 
		 * @param playerFaked
		 * @return
		 */
		public int getNumberOfPlayersChooseYourAns(Player playerFaked)
		{
			int num = 0;
			for (Player player : chosenAnswers.keySet())
				if(playerFaked != player && askChoices.get(chosenAnswers.get(player)).equals(fakeAnswer.get(playerFaked)))
					num++;
			return num;
		}

		public boolean isGotCorrectAnswer(Player player)
		{
			return askChoices.get(chosenAnswers.get(player)).equals(realAnswer.toLowerCase());
		}
	}

	public boolean isGameFinished() {
		return gameFinished;
	}
}
