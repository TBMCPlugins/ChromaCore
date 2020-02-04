package buttondevteam.lib.player;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCYEEHAWEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;

	public TBMCYEEHAWEvent(CommandSender sender) {
		this.sender = sender;
	}

	public CommandSender getSender() {
		return sender;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
