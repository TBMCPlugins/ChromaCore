package buttondevteam.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

public class DebugPotato {
	private List<String> message;
	private String type;

	public void Send(Player player) {
		DebugPotatoAPI.SendDebugPotato(this, player);
	}

	public List<String> getMessage() {
		return message;
	}

	public DebugPotato setMessage(List<String> message) {
		this.message = message;
		return this;
	}

	public DebugPotato setMessage(String message) {
		this.message = WordWrap(message);
		return this;
	}

	public DebugPotato setMessage(String[] message) {
		this.message = Arrays.asList(message);
		return this;
	}

	public String getType() {
		return type;
	}

	public DebugPotato setType(String type) {
		this.type = type;
		return this;
	}

	public static List<String> WordWrap(String message) {
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
