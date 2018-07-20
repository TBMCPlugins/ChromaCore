package buttondevteam.lib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScheduledServerRestartEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

    private final int restartticks;

	public ScheduledServerRestartEvent(int restartticks) {
		this.restartticks = restartticks;
	}

	public int getRestartTicks() {
		return restartticks;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}