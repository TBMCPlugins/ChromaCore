package buttondevteam.lib.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerJoinEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private TBMCPlayerBase player;
	private Player player_;

	public TBMCPlayerJoinEvent(TBMCPlayerBase player, Player player_) {
		this.player = player;
		this.player_ = player_;
	}

	public TBMCPlayerBase GetPlayer() {
		return player;
	}

	public Player getPlayer() { // :P
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