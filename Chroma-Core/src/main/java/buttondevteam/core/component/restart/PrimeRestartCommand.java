package buttondevteam.core.component.restart;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandClass(path = "primerestart", modOnly = true, helpText = {
	"§6---- Prime restart ----", //
	"Restarts the server as soon as nobody is online.", //
	"To be loud, type something after, like /primerestart lol (it doesn't matter what you write)", //
	"To be silent, don't type anything" //
})
@RequiredArgsConstructor
public class PrimeRestartCommand extends ICommand2MC {
	private final RestartComponent component;

	@Command2.Subcommand
	public void def(CommandSender sender, @Command2.TextArg @Command2.OptionalArg String somethingrandom) {
		loud = somethingrandom != null;
		if (Bukkit.getOnlinePlayers().size() > 0) {
			sender.sendMessage("§bPlayers online, restart delayed.");
			if (loud)
				TBMCChatAPI.SendSystemMessage(Channel.globalChat, Channel.RecipientTestResult.ALL, ChatColor.DARK_RED + "The server will restart as soon as nobody is online.", component.getRestartBroadcast());
			plsrestart = true;
		} else {
			sender.sendMessage("§bNobody is online. Restarting now.");
			if (loud)
				TBMCChatAPI.SendSystemMessage(Channel.globalChat, Channel.RecipientTestResult.ALL, "§cNobody is online. Restarting server.", component.getRestartBroadcast());
			Bukkit.spigot().restart();
		}
	}

	@Getter
	private static boolean plsrestart = false;
	@Getter
	private static boolean loud = false;
}
