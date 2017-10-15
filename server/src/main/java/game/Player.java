package game;

import java.io.IOException;

import protocol.ProtocolCallback;
import tokenizer.CommandMessage;

/**
 * A class which holds a single player.
 */
public class Player {
	private String nickName;
	private int score;
	private ProtocolCallback<CommandMessage> callback;
	
	public Player(String nickName, ProtocolCallback<CommandMessage> callback) {
		this.nickName = nickName;
		this.callback = callback;
	}
	
	public void sendMessage(CommandMessage msg) throws IOException {
		callback.sendMessage(msg);
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	public String getNickName() {
		return nickName;
	}
	public int getScore() {
		return score;
	}
}
