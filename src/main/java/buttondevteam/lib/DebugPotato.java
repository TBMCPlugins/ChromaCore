package buttondevteam.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

public class DebugPotato {
	private List<String> message;
	private String type;

	/**
	 * Send the debug potato to a player
	 * 
	 * @param player
	 *            The player
	 */
	public void Send(Player player) {
		DebugPotatoAPI.SendDebugPotato(this, player);
	}

	/**
	 * Get the message (lore of the potato).
	 * 
	 * @return The message
	 */
	public List<String> getMessage() {
		return message;
	}

	/**
	 * Sets the message (lore of the potato).
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(List<String> message) {
		this.message = message;
		return this;
	}

	/**
	 * Sets the message (lore of the potato). It will be word wrapped automatically.
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(String message) {
		this.message = WordWrap(message);
		return this;
	}

	/**
	 * Sets the message (lore of the potato).
	 * 
	 * @param message
	 *            The message
	 * @return This potato
	 */
	public DebugPotato setMessage(String[] message) {
		this.message = Arrays.asList(message);
		return this;
	}

	/**
	 * Gets the type (potato name).
	 * 
	 * @return The type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type (potato name).
	 * 
	 * @param type
	 *            The type
	 * @return This potato
	 */
	public DebugPotato setType(String type) {
		this.type = type;
		return this;
	}

	private static List<String> WordWrap(String message) {
		String[] splitString = message.split("\\s+");
		List<String> newMessage = new ArrayList<String>();
		String currentLine = "";
		int currentLineLength = 0;
		int wordlength;
		int maxLineLength = 40;
		for (String word : splitString) {
			wordlength = word.length();
			if (currentLineLength == 0 || (currentLineLength + wordlength) < maxLineLength) {
				currentLine += word + " ";
				currentLineLength += wordlength + 1;
			} else {
				newMessage.add(currentLine);
				currentLine = word + " ";
				currentLineLength = word.length();
			}
		}
		return newMessage;
	}
}
