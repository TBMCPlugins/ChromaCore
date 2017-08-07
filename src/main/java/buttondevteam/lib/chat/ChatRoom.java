package buttondevteam.lib.chat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ChatRoom extends Channel {
	private List<CommandSender> usersInRoom = new ArrayList<>();

	public ChatRoom(String displayname, Color color, String command) {
		<ChatRoom>super(displayname, color, command, noScoreResult((this_, s) -> this_.usersInRoom.contains(s),
				"Not implemented yet. Please report it to the devs along with which platform you're trying to talk from."));
	}

	public void joinRoom(CommandSender sender) {
		usersInRoom.add(sender);
		TBMCChatAPI.SendSystemMessage(this, 0, sender.getName() + " joined the room");
	}

	public void leaveRoom(CommandSender sender) {
		usersInRoom.remove(sender);
		TBMCChatAPI.SendSystemMessage(this, 0, sender.getName() + " left the room");
	}
}
