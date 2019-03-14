package buttondevteam.core;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.command.CommandSender;

@CommandClass
public class ThorpeCommand extends ICommand2MC {
	@Command2.Subcommand //TODO: Main permissions (groups) like 'mod'
	public void reload(CommandSender sender) {
		if (MainPlugin.Instance.tryReloadConfig())
			sender.sendMessage("§bConfig reloaded.");
		else
			sender.sendMessage("§cFailed to reload config. Check console.");
	}
}
