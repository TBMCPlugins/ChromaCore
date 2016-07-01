package buttondevteam.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerAddEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public TBMCPlayerAddEvent(TBMCPlayer tbmcplayer) {
		// TODO: Separate player configs, figure out how to make one TBMCPlayer
		// object have all the other plugin properties
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
