package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Channel.RecipientTestResult;
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
		if (channel.filteranderrormsg == null)
			return true;
		RecipientTestResult result = channel.filteranderrormsg.apply(sender);
		return result.errormessage == null && score == result.score;
	}

	/**
	 * Note: Errors are sent to the sender automatically
	 */
    public int getMCScore(CommandSender sender) {
        if (channel.filteranderrormsg == null)
            return 0;
        RecipientTestResult result = channel.filteranderrormsg.apply(sender);
        return result.errormessage == null ? result.score : -1;
    }

    /**
     * Note: Errors are sent to the sender automatically
     */
    @Nullable
    public String getGroupID(CommandSender sender) {
        if (channel.filteranderrormsg == null)
            return null;
        RecipientTestResult result = channel.filteranderrormsg.apply(sender);
        return result.errormessage == null ? result.groupID : null;
    }
}
