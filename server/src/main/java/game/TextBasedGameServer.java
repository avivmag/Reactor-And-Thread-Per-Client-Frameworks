package game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import protocol.ProtocolCallback;
import tokenizer.CommandMessage;

/**
 * A text based game server which holds all the data about the game.
 */
public class TextBasedGameServer {
	private Map<String, Player> players;
	private Map<String, Room> rooms;
	private Map<Player, Room> playerToRoom;
	private Map<String, GameFactory> gameFactoriesMap;
	private Object lockGetSupportedGames;

	private static class ServerGameHolder
	{
		private static TextBasedGameServer instance = new TextBasedGameServer(); 
	}

	private TextBasedGameServer()
	{
		players = new ConcurrentHashMap<String, Player>();
		rooms = new ConcurrentHashMap<String, Room>();
		playerToRoom = new ConcurrentHashMap<Player, Room>();
		gameFactoriesMap = new HashMap<String, GameFactory>();
		lockGetSupportedGames = new Object();
	}

	public static TextBasedGameServer getInstance()
	{
		return ServerGameHolder.instance;
	}

	/**
	 * A method which must be called before using this class.
	 * @param supportedGames The supported games of the server
	 */
	public void initialize(Map<String, GameFactory> gameFactoriesMap)
	{
		this.gameFactoriesMap = gameFactoriesMap;
	}
	
	String temp = null;
	public boolean addPlayer(String nickName, ProtocolCallback<CommandMessage> callback)
	{
		synchronized (nickName.intern()) {
			if(players.containsKey(nickName))
				return false;

			players.put(nickName, new Player(nickName, callback));
		}
		return true;
	}

	/**
	 * removes a player, only called when quitting the server.
	 * @param nickname
	 * @throws IOException
	 */
	public void removePlayer(String nickname) throws IOException
	{
		// tries to removes player from the room
		if(players.containsKey(nickname) && playerToRoom.containsKey(players.get(nickname)))
			synchronized(playerToRoom.get(players.get(nickname))){
				playerToRoom.get(players.get(nickname)).removePlayer(players.get(nickname));
			}

		// removes the player from the maps
		if(playerToRoom.containsKey(players.get(nickname)))
			playerToRoom.remove(players.get(nickname));
		players.remove(nickname);
	}

	public CommandMessage enterRoom(String nickName, CommandMessage command) throws IOException
	{
		// if the player is in another room
		if(playerToRoom.get(players.get(nickName)) != null)
		{
			synchronized (playerToRoom.get(players.get(nickName))) {
				// means the player is in another room
				if(playerToRoom.get(players.get(nickName)).isGameInProgress())
					return new CommandMessage("SYSMSG", command.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot exit from " + playerToRoom.get(players.get(nickName)).getName() + " room, game is in progress.");
				// removes the player from the old room he had been
				else
					playerToRoom.get(players.get(nickName)).removePlayer(players.get(nickName));
			}
		}

		// create a new room if there is not an existed room with that name
		if(!rooms.containsKey(command.getParams()))
			synchronized (command.getParams().intern()) {
				if(!rooms.containsKey(command.getParams()))
					rooms.put(command.getParams(), new Room(command.getParams()));
			}

		synchronized (rooms.get(command.getParams())) {
			// if the game is in progress
			if(rooms.get(command.getParams()).isGameInProgress())
				return new CommandMessage("SYSMSG", command.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot join " + command.getParams() + " room, game is in progress.");
			// if the game is not running
			else
			{
				rooms.get(command.getParams()).addPlayer(players.get(nickName));
				playerToRoom.put(players.get(nickName), rooms.get(command.getParams()));
			}
		}
		return new CommandMessage("SYSMSG", command.getCommand() + " " + CommandMessage.Result.ACCEPTED);
	}

	/**
	 * called when a player wants to send some message to other player in the room he is in.
	 * @param sender
	 * @param msg
	 * @return
	 * @throws IOException
	 */
	public CommandMessage sendMessage(String sender, CommandMessage msg) throws IOException
	{
		if(isInRoom(sender))
		{
			playerToRoom.get(players.get(sender)).chatMessage(sender, msg);
			return new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.ACCEPTED);
		}
		else
			return new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without joining room, please use JOIN command.");
	}

	/**
	 * A method which is invoked for a game to start
	 * @param sender
	 * @param msg
	 * @return
	 * @throws IOException
	 */
	public void startGame(String sender, CommandMessage msg) throws IOException
	{
		if(gameFactoriesMap.containsKey(msg.getParams()))
			synchronized (playerToRoom.get(players.get(sender))) {
				playerToRoom.get(players.get(sender)).startGame(players.get(sender), gameFactoriesMap.get(msg.getParams()).create(), msg);
			}
		else
			players.get(sender).sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " game is not supported."));
	}

	/**
	 * Invoked when a player tries to send TXTRESP message.
	 * @param sender
	 * @param response
	 * @throws IOException
	 */
	public void txtrespMessage(String sender, CommandMessage response) throws IOException
	{
		playerToRoom.get(players.get(sender)).txtrespMessage(players.get(sender), response);
	}

	/**
	 * Invoked when a player tries to send SELECTRESP message.
	 * @param sender
	 * @param response
	 * @throws IOException
	 */
	public void selectrespMessage(String sender, CommandMessage response) throws IOException
	{
		playerToRoom.get(players.get(sender)).selectrespMessage(players.get(sender), response);
	}

	/**
	 * Returns a string of supported games
	 * @return
	 */
	public String getSupportedGames()
	{
		synchronized (lockGetSupportedGames) {
			String games = "";
			for (String game : gameFactoriesMap.keySet())
				games += (" " + game);

			return games;
		}
	}

	public boolean isInRoom(String nickName)
	{
		return playerToRoom.containsKey(players.get(nickName));
	}

	public void closeRoom(String roomName)
	{
		synchronized (rooms.get(roomName)) {
			rooms.remove(roomName);
		}
	}
}
