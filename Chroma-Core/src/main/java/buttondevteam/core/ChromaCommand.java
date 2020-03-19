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
		if (plugin == null)
			plugin = MainPlugin.Instance;
		if (!(plugin instanceof ButtonPlugin)) //Probably not a good idea to allow reloading any plugin's config
			sender.sendMessage("§c" + plugin.getName() + " doesn't support this.");
		else if (((ButtonPlugin) plugin).tryReloadConfig())
			sender.sendMessage("§b" + plugin.getName() + " config reloaded.");
		else
			sender.sendMessage("§cFailed to reload config. Check console.");
	}

	@Command2.Subcommand
	public void def(CommandSender sender) {
		sender.sendMessage(ButtonPlugin.getCommand2MC().getCommandsText());
	}
}
