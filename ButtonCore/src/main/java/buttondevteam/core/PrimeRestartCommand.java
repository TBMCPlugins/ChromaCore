package buttondevteam.core;

import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandClass(path = "primerestart", modOnly = true)
public class PrimeRestartCommand extends TBMCCommandBase {
    @Override
    public boolean OnCommand(CommandSender sender, String alias, String[] args) {
	    loud = args.length > 0;
	    if (Bukkit.getOnlinePlayers().size() > 0) {
		    sender.sendMessage("§bPlayers online, restart delayed.");
		    if (loud)
			    Bukkit.broadcastMessage(ChatColor.DARK_RED + "The server will restart as soon as nobody is online.");
		    plsrestart = true;
	    } else {
		    sender.sendMessage("§bNobody is online. Restarting now.");
		    if (loud)
			    Bukkit.broadcastMessage("§cNobody is online. Restarting server.");
		    Bukkit.spigot().restart();
	    }
	    return true;
    }

	@Getter
	private static boolean plsrestart = false;
	@Getter
	private static boolean loud = false;

    @Override
    public String[] GetHelpText(String alias) {
        return new String[]{ //
                "§6---- Prime restart ----", //
		        "Restarts the server as soon as nobody is online.", //
		        "To be loud, type something after, like /primerestart lol (it doesn't matter what you write)", //
		        "To be silent, don't type anything" //
        };
    }
}
