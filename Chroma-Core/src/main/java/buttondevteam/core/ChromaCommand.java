package buttondevteam.core;

import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandClass
public class ChromaCommand extends ICommand2MC {
	@Command2.Subcommand
	public void reload(CommandSender sender, @Command2.OptionalArg Plugin plugin) {
		if(plugin==null)
			plugin=MainPlugin.Instance;
		if(!(plugin instanceof ButtonPlugin))
			plugin.reloadConfig();
		else if (((ButtonPlugin) plugin).tryReloadConfig())
			sender.sendMessage("§b"+plugin.getName()+" config reloaded.");
		else
			sender.sendMessage("§cFailed to reload config. Check console.");
	}
}
