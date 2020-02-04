package buttondevteam.lib.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerLoadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

    private final TBMCPlayerBase player;

	public TBMCPlayerLoadEvent(TBMCPlayerBase player) {
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