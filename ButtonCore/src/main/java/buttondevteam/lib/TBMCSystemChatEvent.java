package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Make sure to only send the message to users who {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCSystemChatEvent extends TBMCChatEventBase {
	private boolean handled;

	public void setHandled() {
		handled = true;
	}

	public TBMCSystemChatEvent(Channel channel, String message, int score, String groupid) { // TODO: Rich message
		super(channel, message, score, groupid);
	}

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
