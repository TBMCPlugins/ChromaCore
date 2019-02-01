package buttondevteam.lib;

import lombok.Getter;
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
public class TBMCCommandPreprocessEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final CommandSender sender;
	@Setter
	private String message;
	@Setter
	private boolean cancelled;

	public TBMCCommandPreprocessEvent(CommandSender sender, String message) {
		this.sender = sender;
		this.message = message; //TODO: Actually call from Discord as well
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
