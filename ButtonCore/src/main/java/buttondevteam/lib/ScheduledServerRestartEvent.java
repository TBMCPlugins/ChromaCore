package buttondevteam.lib;

import buttondevteam.core.component.restart.ScheduledRestartCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class ScheduledServerRestartEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final int restartTicks;
	private final ScheduledRestartCommand command;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}