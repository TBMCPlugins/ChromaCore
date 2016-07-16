package buttondevteam.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event) {
		try {
			TBMCPlayer.LoadPlayer(event.getPlayer().getUniqueId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
