package buttondevteam.player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerLoadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private YamlConfiguration config;

	public TBMCPlayerLoadEvent(YamlConfiguration yc) {
		this.config = yc;
	}

	public YamlConfiguration GetPlayerConfig() {
		return config;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
