package buttondevteam.core.component.restart;

import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.ScheduledServerRestartEvent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;

@CommandClass(modOnly = true, path = "schrestart", helpText = {
	"Scheduled restart", //
	"This command restarts the server 1 minute after it's executed, warning players every 10 seconds.", //
	"You can optionally set the amount of seconds to wait before the restart." //
})
@RequiredArgsConstructor
public class ScheduledRestartCommand extends ICommand2MC {
	@Getter
	@Setter
	private int restartCounter;
	private BukkitTask restarttask;
	private volatile BossBar restartbar;
	@Getter
	@Nonnull
	private final RestartComponent component;

	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.OptionalArg int seconds) {
		if (seconds == 0) seconds = 60;
		if (seconds < 10) {
			sender.sendMessage("§cError: Seconds must be at least 10.");
			return false;
		}
		final int restarttime = restartCounter = seconds * 20;
		restartbar = Bukkit.createBossBar("Server restart in " + seconds + "s", BarColor.RED, BarStyle.SOLID,
			BarFlag.DARKEN_SKY);
		restartbar.setProgress(1);
		Bukkit.getOnlinePlayers().forEach(p -> restartbar.addPlayer(p));
		sender.sendMessage("Scheduled restart in " + seconds);
		ScheduledServerRestartEvent e = new ScheduledServerRestartEvent(restarttime, this);
		Bukkit.getPluginManager().callEvent(e);
		restarttask = Bukkit.getScheduler().runTaskTimer(MainPlugin.instance, () -> {
			if (restartCounter < 0) {
				restarttask.cancel();
				restartbar.getPlayers().forEach(p -> restartbar.removePlayer(p));
				Bukkit.spigot().restart();
			}
			if (restartCounter % 200 == 0 && Bukkit.getOnlinePlayers().size() > 0)
				TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, "§c-- The server is restarting in " + restartCounter / 20 + " seconds!", component.getRestartBroadcast());
			restartbar.setProgress(restartCounter / (double) restarttime);
			restartbar.setTitle(String.format("Server restart in %.2f", restartCounter / 20f));
			restartCounter--;
		}, 1, 1);
		return true;
	}
}
