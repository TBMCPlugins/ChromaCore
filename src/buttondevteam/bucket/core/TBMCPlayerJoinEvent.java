package buttondevteam.bucket.core;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerJoinEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayer player;

	public TBMCPlayerJoinEvent(TBMCPlayer player) {
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
