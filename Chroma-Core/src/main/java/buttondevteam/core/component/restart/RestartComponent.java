package buttondevteam.core.component.restart;

import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.IFakePlayer;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Provides commands such as /schrestart (restart after a countdown) and /primerestart (restart when nobody is online).
 * Also can automatically restart at a given time.
 */
@ComponentMetadata(enabledByDefault = false)
public class RestartComponent extends Component<MainPlugin> implements Listener {
	@Override
	public void enable() {
		var scheduledRestartCommand = new ScheduledRestartCommand(this);
		registerCommand(scheduledRestartCommand);
		registerCommand(new PrimeRestartCommand(this));
		registerListener(this);
		restartBroadcast = TBMCSystemChatEvent.BroadcastTarget.add("restartCountdown");

		int restartAt = this.restartAt.get();
		if (restartAt < 0) return;
		int restart = syncStart(restartAt);
		log("Scheduled restart " + (restart / 3600. / 20.) + " hours from now");
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> scheduledRestartCommand.def(Bukkit.getConsoleSender(), 0), restart);
	}

	@Override
	public void disable() {
		TBMCSystemChatEvent.BroadcastTarget.remove(restartBroadcast);
	}

	/**
	 * Specifies the hour of day when the server should be restarted. Set to -1 to disable.
	 */
	private final ConfigData<Integer> restartAt = getConfig().getData("restartAt", 12);

	private long lasttime = 0;
	@Getter
	private TBMCSystemChatEvent.BroadcastTarget restartBroadcast;

	private int syncStart(int hour) {
		var now = ZonedDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC));
		int secs = now.getHour() * 3600 + now.getMinute() * 60 + now.getSecond();
		//System.out.println("now: " + secs / 3600.);
		int diff = secs - hour * 3600;
		//System.out.println("diff: " + diff / 3600.);
		if (diff < 0) {
			diff += 24 * 3600;
		}
		//System.out.println("diff: " + diff / 3600.);
		int count = diff / (24 * 3600);
		//System.out.println("count: " + count);
		int intervalPart = diff - count * 24 * 3600;
		//System.out.println("intervalPart: " + intervalPart / 3600.);
		int remaining = 24 * 3600 - intervalPart;
		//System.out.println("remaining: " + remaining / 3600.);
		return remaining * 20;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (PrimeRestartCommand.isPlsrestart()
			&& !event.getQuitMessage().equalsIgnoreCase("Server closed")
			&& !event.getQuitMessage().equalsIgnoreCase("Server is restarting")) {
			if (Bukkit.getOnlinePlayers().size() <= 1) {
				if (PrimeRestartCommand.isLoud())
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, "§cNobody is online anymore. Restarting.", restartBroadcast);
				Bukkit.spigot().restart();
			} else if (!(event.getPlayer() instanceof IFakePlayer) && System.nanoTime() - 10 * 60 * 1000000000L - lasttime > 0) { //10 minutes passed since last reminder
				lasttime = System.nanoTime();
				if (PrimeRestartCommand.isLoud())
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, ChatColor.DARK_RED + "The server will restart as soon as nobody is online.", restartBroadcast);
			}
		}
	}
}
