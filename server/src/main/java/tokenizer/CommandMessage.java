package tokenizer;

/**
 * A message which holds a command and some parameters
 */
public class CommandMessage implements Message<CommandMessage>{
	private String command;
	private String params;

	public CommandMessage(String command, String params) {
		this.command = command;
		this.params = params;
	}
	
	public String getMessage(){
		return command + " " + params;
	}
	public String getCommand() {
		return command;
	}
	public String getParams() {
		return params;
	}
	public enum Result { ACCEPTED, REJECTED, UNIDENTIFIED };
}
