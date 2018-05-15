package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Make sure to only send the message to users where {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCChatEvent extends TBMCChatEventBase {
	public TBMCChatEvent(CommandSender sender, Channel channel, String message, int score, boolean fromcmd) {
		super(channel, message, score);
		this.sender = sender;
		this.fromcmd = fromcmd;
	}

	private static final HandlerList handlers = new HandlerList();

	private CommandSender sender;
	private boolean fromcmd;
	// TODO: Message object with data?

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
