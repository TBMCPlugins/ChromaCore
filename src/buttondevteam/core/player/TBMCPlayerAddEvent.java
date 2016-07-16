package buttondevteam.core.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerAddEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayer player;

	public TBMCPlayerAddEvent(TBMCPlayer player) {
		//TODO: Convert player configs
		this.player = player;
	}

	public TBMCPlayer GetPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
