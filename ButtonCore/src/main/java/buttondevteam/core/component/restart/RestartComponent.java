package buttondevteam.core.component.restart;

import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.IFakePlayer;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Provides commands such as /schrestart (restart after a countdown) and /primerestart (restart when nobody is online)
 */
public class RestartComponent extends Component<MainPlugin> implements Listener {
	@Override
	public void enable() {
		//TODO: Permissions for the commands
		registerCommand(new ScheduledRestartCommand(this));
		TBMCChatAPI.AddCommand(this, new PrimeRestartCommand(this));
		registerListener(this);
		restartBroadcast = TBMCSystemChatEvent.BroadcastTarget.add("restartCountdown");
	}

	@Override
	public void disable() {
		TBMCSystemChatEvent.BroadcastTarget.remove(restartBroadcast);
	}

	private long lasttime = 0;
	@Getter
	private TBMCSystemChatEvent.BroadcastTarget restartBroadcast;

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (PrimeRestartCommand.isPlsrestart()
			&& !event.getQuitMessage().equalsIgnoreCase("Server closed")
			&& !event.getQuitMessage().equalsIgnoreCase("Server is restarting")) {
			if (Bukkit.getOnlinePlayers().size() <= 1) {
				if (PrimeRestartCommand.isLoud())
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, "§cNobody is online anymore. Restarting.", restartBroadcast);
				Bukkit.spigot().restart();
			} else if (!(event.getPlayer() instanceof IFakePlayer) && System.nanoTime() - 10 * 1000000000L - lasttime > 0) { //Ten seconds passed since last reminder
				lasttime = System.nanoTime();
				if (PrimeRestartCommand.isLoud())
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, ChatColor.DARK_RED + "The server will restart as soon as nobody is online.", restartBroadcast);
			}
		}
	}
}
