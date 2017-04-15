package buttondevteam.lib.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerJoinEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayerBase player;

	public TBMCPlayerJoinEvent(TBMCPlayerBase player) {
		this.player = player;
	}

	public TBMCPlayerBase GetPlayer() {
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