package buttondevteam.core.component.restart;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.IFakePlayer;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class RestartComponent extends Component implements Listener {
	@Override
	public void enable() {
		//TODO: Permissions for the commands
		TBMCChatAPI.AddCommand(this, new ScheduledRestartCommand());
		TBMCChatAPI.AddCommand(this, new PrimeRestartCommand());
		registerListener(this);
	}

	@Override
	public void disable() {

	}

	private long lasttime = 0;

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
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
}
