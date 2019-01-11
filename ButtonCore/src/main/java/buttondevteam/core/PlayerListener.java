package buttondevteam.core;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.IFakePlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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
            MainPlugin.Instance.getLogger().info("Added " + event.getPlayer().getName() + " as a member.");
        }
	}

    private long lasttime = 0;
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.quitPlayer(event.getPlayer());
        if (PrimeRestartCommand.isPlsrestart()
                && !event.getQuitMessage().equalsIgnoreCase("Server closed")
                && !event.getQuitMessage().equalsIgnoreCase("Server is restarting")) {
            if (Bukkit.getOnlinePlayers().size() <= 1) {
	            if (PrimeRestartCommand.isLoud())
		            Bukkit.broadcastMessage("§cNobody is online anymore. Restarting.");
                Bukkit.spigot().restart();
            } else if (!(event.getPlayer() instanceof IFakePlayer) && System.nanoTime() - 10 * 1000000000L - lasttime > 0) { //Ten seconds passed since last reminder
                lasttime = System.nanoTime();
	            if (PrimeRestartCommand.isLoud())
		            Bukkit.broadcastMessage(ChatColor.DARK_RED + "The server will restart as soon as nobody is online.");
            }
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSystemChat(TBMCSystemChatEvent event) {
        if (event.isHandled())
            return; // Only handle here if ButtonChat couldn't
		if (Arrays.stream(event.getExceptions()).anyMatch("Minecraft"::equalsIgnoreCase))
			return;
        Bukkit.getOnlinePlayers().stream().filter(event::shouldSendTo)
	        .forEach(p -> p.sendMessage(event.getChannel().DisplayName().get().substring(0, 2) + event.getMessage()));
    }
}