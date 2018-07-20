package buttondevteam.lib.chat;

import buttondevteam.lib.player.ChromaGamerBase;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

@Builder
@Getter
public class ChatMessage {
    /**
     * The MC channel to send the message to.
     */
    private final Channel channel;
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
    private final String message;
    /**
     * Indicates whether the message comes from running a command (like /tableflip). Implemented to be used from Discord.
     */
    private boolean fromCommand;
    /**
     * The sender which we should check for permissions. Same as {@link #sender} by default.
     */
    private CommandSender permCheck;

    private static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }

    @NonNull
    public static ChatMessageBuilder builder(Channel channel, CommandSender sender, ChromaGamerBase user, String message) {
        return builder().channel(channel).sender(sender).user(user).message(message);
    }

}
