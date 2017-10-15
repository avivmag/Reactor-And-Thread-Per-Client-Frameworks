package game;

import java.io.IOException;
import java.util.List;

import tokenizer.CommandMessage;

/**
 * An interface for all games which is supported by this package.
 */
public interface Game {
	/**
	 * Launch new game
	 * @param players
	 * @throws IOException
	 */
	void startGame(List<Player> players) throws IOException;
	/**
	 * the player sent TXTRESP message
	 * @param sender
	 * @param response His original message
	 * @throws IOException
	 */
	void txtrespMessage(Player sender, CommandMessage response) throws IOException;
	/**
	 * the player sent SELECTRESP message
	 * @param sender
	 * @param response His original message
	 * @throws IOException
	 */
	void selectrespMessage(Player sender, CommandMessage response) throws IOException;
	/**
	 * check if the current game has been finished
	 * @return
	 */
	boolean isGameFinished();
	
	/**
	 * Removes the player from the room.
	 * @param player
	 */
	void removePlayer(Player player) throws IOException;
}
