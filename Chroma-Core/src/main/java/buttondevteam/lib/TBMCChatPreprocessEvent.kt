package buttondevteam.lib;

import buttondevteam.core.component.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Can be used to change messages before it's sent.
 * <b>Only called before sending messages with SendChatMessage.</b>
 * 
 * @author NorbiPeti
 *
 */
@Getter
public class TBMCChatPreprocessEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

    private final Channel channel;
    private final CommandSender sender;
    @Setter
	private String message;
    @Setter
    private boolean cancelled;

	public TBMCChatPreprocessEvent(CommandSender sender, Channel channel, String message) {
		super(true);
		this.sender = sender;
		this.channel = channel;
		this.message = message;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
