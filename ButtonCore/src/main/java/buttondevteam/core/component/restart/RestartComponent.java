package buttondevteam.core.component.restart;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
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
		TBMCChatAPI.AddCommand(this, new ScheduledRestartCommand(this));
		TBMCChatAPI.AddCommand(this, new PrimeRestartCommand(this));
		registerListener(this);
		restartBroadcast = TBMCSystemChatEvent.BroadcastTarget.add("restartCountdown");
	}

	@Override
	public void disable() {
		TBMCSystemChatEvent.BroadcastTarget.remove(restartBroadcast);
	}

	private long lasttime = 0;
	TBMCSystemChatEvent.BroadcastTarget restartBroadcast;

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
