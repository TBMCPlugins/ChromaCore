package buttondevteam.core;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;

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
	        return; // Only handle here if ButtonChat couldn't - ButtonChat doesn't even handle this
		if (Arrays.stream(event.getExceptions()).anyMatch("Minecraft"::equalsIgnoreCase))
			return;
        Bukkit.getOnlinePlayers().stream().filter(event::shouldSendTo)
	        .forEach(p -> p.sendMessage(event.getChannel().DisplayName().get().substring(0, 2) + event.getMessage()));
    }
}