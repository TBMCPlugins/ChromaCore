package buttondevteam.core.component.restart;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandClass(path = "primerestart", modOnly = true)
@RequiredArgsConstructor
public class PrimeRestartCommand extends TBMCCommandBase {
	private final RestartComponent component;
    @Override
    public boolean OnCommand(CommandSender sender, String alias, String[] args) {
	    loud = args.length > 0;
	    if (Bukkit.getOnlinePlayers().size() > 0) {
		    sender.sendMessage("§bPlayers online, restart delayed.");
		    if (loud)
			    TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, ChatColor.DARK_RED + "The server will restart as soon as nobody is online.", component.restartBroadcast);
		    plsrestart = true;
	    } else {
		    sender.sendMessage("§bNobody is online. Restarting now.");
		    if (loud)
			    TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, "§cNobody is online. Restarting server.", component.restartBroadcast);
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
