package buttondevteam.lib;

import buttondevteam.core.component.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Can be used to change or handle commands before they're sent.
 * <b>Called on using player, console and Discord commands.</b>
 *
 * @author NorbiPeti
 */
@Getter
@RequiredArgsConstructor
public class TBMCCommandPreprocessEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final CommandSender sender;
	private final Channel channel;
	@Setter
	private final String message;
	private final CommandSender permCheck;
	@Setter
	private boolean cancelled;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
