package protocol;

import game.TextBasedGameServer;
import tokenizer.CommandMessage;

/**
 * a simple implementation of the server protocol interface
 */
public class TBGSProtocol implements AsyncServerProtocol<CommandMessage> {

	private boolean _shouldClose = false;
	private boolean _connectionTerminated = false;
	private String nickName;

	/**
	 * processes a message<BR>
	 * this simple interface prints the message to the screen, then composes a simple
	 * reply and sends it back to the client
	 *
	 * @param msg the message to process
	 * @return the reply that should be sent to the client, or null if no reply needed
	 */
	@Override
	public void processMessage(CommandMessage msg, ProtocolCallback<CommandMessage> callback) {
		try{
			if (isEnd(msg) || _connectionTerminated){
				if(nickName != null)
					TextBasedGameServer.getInstance().removePlayer(nickName);
				nickName = null;
				callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.ACCEPTED));
			}
			else
				switch(msg.getCommand().toLowerCase())
				{
				// tries to add a player to the server.
				case "nick":
					if(nickName == null)
						if(!msg.getParams().equals(""))
							if(TextBasedGameServer.getInstance().addPlayer(msg.getParams(), callback))
							{
								nickName = msg.getParams();
								callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.ACCEPTED));
							}
							else
								callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " '" + msg.getParams() + "' is already taken"));
						else
							callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " please use NICK with a following name"));
					else
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot change nick"));
					break;
					// tries to join a player to room.
				case "join":
					if(!msg.getParams().equals(""))
						if(nickName != null)
							callback.sendMessage(TextBasedGameServer.getInstance().enterRoom(nickName, msg));
						else
							callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot join any room without nickname, please use NICK command."));
					else
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " please use JOIN with a following room"));
					break;
					// tries to send message to other players on the same room.
				case "msg":
					if(nickName == null)
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without nickname and joining room, please use NICK command."));
					else
						callback.sendMessage(TextBasedGameServer.getInstance().sendMessage(nickName, msg));
					break;
					// tries to list available games.
				case "listgames":
					callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.ACCEPTED + TextBasedGameServer.getInstance().getSupportedGames()));
					break;
					// tries to start a game.
				case "startgame":
					if(nickName == null)
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without nickname and joining room, please use NICK command."));
					else if(!TextBasedGameServer.getInstance().isInRoom(nickName))
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without joining room, please use JOIN command."));
					else
						TextBasedGameServer.getInstance().startGame(nickName, msg);
					break;
					// tries to insert a text response.
				case "txtresp":
					if(nickName == null)
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without nickname and joining room, please use NICK command."));
					else if(!TextBasedGameServer.getInstance().isInRoom(nickName))
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without joining room, please use JOIN command."));
					else
						TextBasedGameServer.getInstance().txtrespMessage(nickName, msg);
					break;
					// tries to insert a select response. 
				case "selectresp":
					if(nickName == null)
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without nickname and joining room, please use NICK command."));
					else if(!TextBasedGameServer.getInstance().isInRoom(nickName))
						callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.REJECTED + " cannot send message without joining room, please use JOIN command."));
					else
						TextBasedGameServer.getInstance().selectrespMessage(nickName, msg);
					break;
					// If there is no supported command of this kind.
				default:
					callback.sendMessage(new CommandMessage("SYSMSG", msg.getCommand() + " " + CommandMessage.Result.UNIDENTIFIED));
					return;
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * determine whether the given message is the termination message
	 *
	 * @param msg the message to examine
	 * @return false - this simple protocol doesn't allow termination...
	 */
	@Override
	public boolean isEnd(CommandMessage msg) {
		boolean ans = msg.getCommand().toLowerCase().equals("quit");
		if (ans)
			_shouldClose = true;
		return ans;
	}

	/**
	 * Is the protocol in a closing state?.
	 * When a protocol is in a closing state, it's handler should write out all pending data, 
	 * and close the connection.
	 * @return true if the protocol is in closing state.
	 */
	@Override
	public boolean shouldClose() {
		return this._shouldClose;
	}

	/**
	 * Indicate to the protocol that the client disconnected.
	 */
	@Override
	public void connectionTerminated() {
		this._connectionTerminated = true;
	}
}
