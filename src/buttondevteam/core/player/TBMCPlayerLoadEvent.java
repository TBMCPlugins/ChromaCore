package buttondevteam.core.player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCPlayerLoadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private YamlConfiguration yaml;
	private TBMCPlayer player;

	public TBMCPlayerLoadEvent(YamlConfiguration yaml, TBMCPlayer player) {
		//TODO: Convert player configs
		this.yaml = yaml;
		this.player = player;
	}

	public YamlConfiguration GetPlayerConfig() {
		return yaml;
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
