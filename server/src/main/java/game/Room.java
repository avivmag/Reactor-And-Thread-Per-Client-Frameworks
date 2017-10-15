package game;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import tokenizer.CommandMessage;

/**
 * A single room which holds players and a game
 */
public class Room {
	private List<Player> players;
	private String name;
	private Game game;
	private final int INITIAL_SCORE = 0;

	public Room(String name) {
		players = new CopyOnWriteArrayList<Player>();
		this.name = name;
	}

	/**
	 * Starts a new game
	 * @param game the game you want to start
	 * @param msg 
	 * @return
	 * @throws IOException
	 */
	public void startGame(Player sender, Game game, CommandMessage msg) throws IOException
	{
		if(this.game == null || this.game.isGameFinished() )
		{
			for (Player player : players)
				player.setScore(INITIAL_SCORE);
			
			this.game = game;
			sender.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.ACCEPTED));
			game.startGame(players);
		}
		else
			sender.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " game is in progress."));
	}
	
	/**
	 * Sends new TXTRESP message.
	 * @param sender
	 * @param response
	 * @throws IOException
	 */
	public void txtrespMessage(Player sender, CommandMessage response) throws IOException
	{
		game.txtrespMessage(sender, response);
	}
	
	/**
	 * Sends new SELECTRESP message.
	 * @param sender
	 * @param response
	 * @throws IOException
	 */
	public void selectrespMessage(Player sender, CommandMessage response) throws IOException
	{
		game.selectrespMessage(sender, response);
	}
	
	public boolean isGameInProgress()
	{
		return (game != null && !game.isGameFinished());
	}
	
	public void addPlayer(Player player) {
		players.add(player);
	}
	
	public void removePlayer(Player player) throws IOException
	{		
		if(game != null)
			game.removePlayer(player);
		players.remove(player);
		if(players.size() == 0)
			TextBasedGameServer.getInstance().closeRoom(name);
	}

	/**
	 * sends message from some sender to all other players in room
	 * @param sender
	 * @param msg
	 * @throws IOException
	 */
	public void chatMessage(String sender, CommandMessage msg) throws IOException
	{
		for (Player player : players)
			if(!player.getNickName().equals(sender))
				player.sendMessage(new CommandMessage("USRMSG", sender + ": " + msg.getParams()));
		
	}

	public List<Player> getPlayers() {
		return players;
	}

	public String getName() {
		return name;
	}

	public Game getGame() {
		return game;
	}
}
