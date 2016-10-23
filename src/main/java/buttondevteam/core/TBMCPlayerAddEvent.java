package buttondevteam.bucket;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>
 * This event gets called when a new player joins. After this event, the
 * {@link TBMCPlayerSaveEvent} will be called.
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCPlayerAddEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayer player;

	public TBMCPlayerAddEvent(TBMCPlayer player) {
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
