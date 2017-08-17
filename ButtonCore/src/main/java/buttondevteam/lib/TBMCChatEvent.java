package buttondevteam.lib;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import buttondevteam.lib.chat.Channel;
import lombok.Getter;

/**
 * Make sure to only send the message to users who {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCChatEvent extends TBMCChatEventBase {
	public TBMCChatEvent(CommandSender sender, Channel channel, String message, int score) {
		super(channel, message, score);
		this.sender = sender;
	}

	private static final HandlerList handlers = new HandlerList();

	private CommandSender sender;
	// TODO: Message object with data?

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
