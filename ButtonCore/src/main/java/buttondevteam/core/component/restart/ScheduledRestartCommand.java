package buttondevteam.core.component.restart;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.ScheduledServerRestartEvent;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

@CommandClass(modOnly = true, path = "schrestart")
public class ScheduledRestartCommand extends TBMCCommandBase {
	@Getter
	@Setter
	private int restartCounter;
	private BukkitTask restarttask;
	private volatile BossBar restartbar;

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		int secs = 60;
		try {
			if (args.length > 0)
				secs = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage("§cError: Seconds must be a number.");
            return false;
		}
		if (secs < 10) {
			sender.sendMessage("§cError: Seconds must be at least 10.");
			return false;
		}
		final int restarttime = restartCounter = secs * 20;
		restartbar = Bukkit.createBossBar("Server restart in " + secs + "s", BarColor.RED, BarStyle.SOLID,
				BarFlag.DARKEN_SKY);
		restartbar.setProgress(1);
        Bukkit.getOnlinePlayers().forEach(p -> restartbar.addPlayer(p));
		sender.sendMessage("Scheduled restart in " + secs);
		ScheduledServerRestartEvent e = new ScheduledServerRestartEvent(restarttime, this);
		Bukkit.getPluginManager().callEvent(e);
		restarttask = Bukkit.getScheduler().runTaskTimer(MainPlugin.Instance, () -> {
			if (restartCounter < 0) {
				restarttask.cancel();
                restartbar.getPlayers().forEach(p -> restartbar.removePlayer(p));
				Bukkit.spigot().restart();
			}
			if (restartCounter % 200 == 0)
				Bukkit.broadcastMessage("§c-- The server is restarting in " + restartCounter / 20 + " seconds! (/press)");
			restartbar.setProgress(restartCounter / (double) restarttime);
			restartbar.setTitle(String.format("Server restart in %.2f", restartCounter / 20f));
			restartCounter--;
		}, 1, 1);
		return true;
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- Scheduled restart ----", //
				"This command restarts the server 1 minute after it's executed, warning players every 10 seconds.", //
				"You can optionally set the amount of ticks to wait before the restart." //
		};
	}
}
