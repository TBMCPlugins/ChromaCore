package buttondevteam.core;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.player.TBMCPlayerBase;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		TBMCPlayerBase.joinPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.quitPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSystemChat(TBMCSystemChatEvent event) {
		if (event.isHandled())
			return; // Only handle here if ButtonChat couldn't
		Bukkit.getOnlinePlayers().stream().filter(p -> event.shouldSendTo(p))
				.forEach(p -> p.sendMessage(event.getChannel().DisplayName.substring(0, 2) + event.getMessage()));
	}
}
