package buttondevteam.core;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import buttondevteam.lib.ScheduledServerRestartEvent;
import buttondevteam.lib.chat.TBMCCommandBase;

public class ScheduledRestartCommand extends TBMCCommandBase {
	private static volatile int restartcounter;
	private static volatile BukkitTask restarttask;
	private static volatile BossBar restartbar;

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		int ticks = 20 * 60;
		try {
			if (args.length > 0)
				ticks = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
		}
		if (ticks < 20) {
			sender.sendMessage("§cError: Ticks must be more than 20.");
			return false;
		}
		final int restarttime = restartcounter = ticks;
		restartbar = Bukkit.createBossBar("Server restart in " + ticks / 20f, BarColor.RED, BarStyle.SOLID,
				BarFlag.DARKEN_SKY);
		restartbar.setProgress(1);
		Bukkit.getOnlinePlayers().stream().forEach(p -> restartbar.addPlayer(p));
		sender.sendMessage("Scheduled restart in " + ticks / 20f);
		ScheduledServerRestartEvent e = new ScheduledServerRestartEvent(ticks);
		Bukkit.getPluginManager().callEvent(e);
		restarttask = Bukkit.getScheduler().runTaskTimerAsynchronously(MainPlugin.Instance, () -> {
			if (restartcounter < 0) {
				restarttask.cancel();
				restartbar.getPlayers().stream().forEach(p -> restartbar.removePlayer(p));
				Bukkit.spigot().restart();
			}
			if (restartcounter % 200 == 0)
				Bukkit.broadcastMessage("§c-- The server is restarting in " + restartcounter);
			restartbar.setProgress(restartcounter / restarttime);
			restartcounter--;
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

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

	@Override
	public boolean GetModOnly() {
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "schrestart";
	}
}
