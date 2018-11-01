package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public abstract class TBMCChatEventBase extends Event implements Cancellable {
	private final Channel channel;
	private @NonNull String message;
	private @Setter boolean cancelled;
    /**
     * The sender's score.
     */
    private final int score;
    /**
     * The sender's group ID.
     */
    private final String groupID;

	/**
	 * Note: Errors are sent to the sender automatically
	 */
	public boolean shouldSendTo(CommandSender sender) {
		return channel.shouldSendTo(sender, score);
	}

	/**
	 * Note: Errors are sent to the sender automatically
	 */
    public int getMCScore(CommandSender sender) {
	    return channel.getMCScore(sender);
    }

    /**
     * Note: Errors are sent to the sender automatically<br>
     *
     * Null means don't send
     */
    @Nullable
    public String getGroupID(CommandSender sender) {
	    return channel.getGroupID(sender); //TODO: Performance-wise it'd be much better to use serialization for player data - it's only converted once
    }
}
