package buttondevteam.lib.chat;

import buttondevteam.lib.player.ChromaGamerBase;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.command.CommandSender;

@Builder
@Getter
public class ChatMessage {
	/**
	 * The sender which sends the message.
	 */
	private final CommandSender sender;
	/**
	 * The Chroma user which sends the message.
	 */
	private final ChromaGamerBase user;
	/**
	 * The message to send as the user.
	 */
	@Setter
	private String message;
	/**
	 * Indicates whether the message comes from running a command (like /tableflip). Implemented to be used from Discord.
	 */
	private boolean fromCommand;
	/**
	 * The sender which we should check for permissions. Same as {@link #sender} by default.
	 */
	private CommandSender permCheck;
	/**
	 * The origin of the message, "Minecraft" or "Discord" for example. May be displayed to the user.<br>
	 *     <b>This is the user class capitalized folder name.</b>
	 */
	private final String origin;

	/**
	 * The sender which we should check for permissions. Same as {@link #sender} by default.
	 *
	 * @return The perm check or the sender
	 */
	public CommandSender getPermCheck() {
		return permCheck == null ? sender : permCheck;
	}

	private static ChatMessageBuilder builder() {
		return new ChatMessageBuilder();
	}

	@NonNull
	public static ChatMessageBuilder builder(CommandSender sender, ChromaGamerBase user, String message) {
		return builder().sender(sender).user(user).message(message).origin(user.getFolder().substring(0, 1).toUpperCase() + user.getFolder().substring(1));
	}
}
