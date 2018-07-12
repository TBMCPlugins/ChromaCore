package buttondevteam.lib.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerQuitEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final TBMCPlayerBase player;
	private final Player player_;

	public TBMCPlayerQuitEvent(TBMCPlayerBase player, Player player_) {
		this.player = player;
		this.player_ = player_;
	}

	public TBMCPlayerBase GetPlayer() {
		return player;
	}

	public Player getPlayer() {
		return player_;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}