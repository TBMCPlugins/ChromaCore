package buttondevteam.bucket;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerQuitEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayer player;

	public TBMCPlayerQuitEvent(TBMCPlayer player) {
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
