package buttondevteam.bucket;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		TBMCPlayer player = TBMCPlayer.LoadPlayer(event.getPlayer());
		if (player == null)
			event.getPlayer().sendMessage("§c[TBMC] Failed to load player data! Please contact a mod.");
		else
			TBMCPlayer.JoinPlayer(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayer player = TBMCPlayer.GetPlayer(event.getPlayer());
		TBMCPlayer.SavePlayer(player);
		TBMCPlayer.QuitPlayer(player);
	}
}
