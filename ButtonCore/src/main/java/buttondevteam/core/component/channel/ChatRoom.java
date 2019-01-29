package buttondevteam.core.component.channel;

import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom extends Channel {
    private final List<CommandSender> usersInRoom = new ArrayList<>();

	public ChatRoom(String displayname, Color color, String command) {
		<ChatRoom>super(displayname, color, command, noScoreResult((this_, s) -> this_.usersInRoom.contains(s),
				"Not implemented yet. Please report it to the devs along with which platform you're trying to talk from."));
	}

	public void joinRoom(CommandSender sender) {
		usersInRoom.add(sender);
		TBMCChatAPI.SendSystemMessage(this, RecipientTestResult.ALL, sender.getName() + " joined the room");
	}

	public void leaveRoom(CommandSender sender) {
		usersInRoom.remove(sender);
		TBMCChatAPI.SendSystemMessage(this, RecipientTestResult.ALL, sender.getName() + " left the room");
	}
}
