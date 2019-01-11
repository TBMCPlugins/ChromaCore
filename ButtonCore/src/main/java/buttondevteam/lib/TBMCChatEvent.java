package buttondevteam.lib;

import buttondevteam.component.channel.Channel;
import buttondevteam.lib.chat.ChatMessage;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Make sure to only send the message to users where {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCChatEvent extends TBMCChatEventBase {
	public TBMCChatEvent(Channel channel, ChatMessage cm, Channel.RecipientTestResult rtr) {
		super(channel, cm.getMessage(), rtr.score, rtr.groupID);
		this.cm = cm;
    }

	private static final HandlerList handlers = new HandlerList();

	@Delegate //<-- Backwards compatibility
	private ChatMessage cm;

	private boolean isIgnoreSenderPermissions() {
		return cm.getPermCheck() != cm.getSender();
	}
	// TODO: Message object with data?

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Override
    public boolean shouldSendTo(CommandSender sender) {
	    if (isIgnoreSenderPermissions() && sender.equals(this.cm.getSender()))
            return true; //Allow sending the message no matter what
        return super.shouldSendTo(sender);
    }

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Override
    public int getMCScore(CommandSender sender) {
	    if (isIgnoreSenderPermissions() && sender.equals(this.cm.getSender()))
            return getScore(); //Send in the correct group no matter what
        return super.getMCScore(sender);
    }

    /**
     * This will allow the sender of the message if {@link #isIgnoreSenderPermissions()} is true.
     */
    @Nullable
    @Override
    public String getGroupID(CommandSender sender) {
	    if (isIgnoreSenderPermissions() && sender.equals(this.cm.getSender()))
            return getGroupID(); //Send in the correct group no matter what
        return super.getGroupID(sender);
    }

    @Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
