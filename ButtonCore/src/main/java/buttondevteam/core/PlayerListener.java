package buttondevteam.core;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static buttondevteam.core.MainPlugin.permission;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		TBMCPlayerBase.joinPlayer(event.getPlayer());
        if (permission != null && !permission.playerInGroup(event.getPlayer(), "member")
                && (new Date(event.getPlayer().getFirstPlayed()).toInstant().plus(7, ChronoUnit.DAYS).isBefore(Instant.now())
                || event.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) > 20 * 3600 * 12)) {
            permission.playerAddGroup(null, event.getPlayer(), "member");
            event.getPlayer().sendMessage("§bYou are a member now. YEEHAW");
        }
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.quitPlayer(event.getPlayer());
		if (PrimeRestartCommand.isPlsrestart() && Bukkit.getOnlinePlayers().size() <= 1) {
			Bukkit.broadcastMessage("§cNobody is online anymore. Restarting.");
			Bukkit.spigot().restart();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSystemChat(TBMCSystemChatEvent event) {
		if (event.isHandled())
			return; // Only handle here if ButtonChat couldn't
		Bukkit.getOnlinePlayers().stream().filter(p -> event.shouldSendTo(p))
				.forEach(p -> p.sendMessage(event.getChannel().DisplayName.substring(0, 2) + event.getMessage()));
	}
}