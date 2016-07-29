package buttondevteam.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		if (TBMCPlayer.LoadPlayer(event.getPlayer()) == null)
			event.getPlayer().sendMessage("§c[TBMC] Failed to load player data! Please contact a mod.");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayer.SavePlayer(TBMCPlayer.GetPlayer(event.getPlayer()));
	}
}
