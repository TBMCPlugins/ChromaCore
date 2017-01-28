package buttondevteam.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import buttondevteam.lib.player.TBMCPlayerBase;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		TBMCPlayerBase.joinPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.quitPlayer(event.getPlayer());
	}
}
