package buttondevteam.core.component.updater;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

@CommandClass(modOnly = true)
public class UpdatePluginCommand extends ICommand2MC {
	public void def(CommandSender sender, @Command2.OptionalArg String plugin, @Command2.OptionalArg String branch) {
		Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
			if (plugin == null) {
				sender.sendMessage("Downloading plugin names...");
				boolean first = true;
				for (String plugin2 : PluginUpdater.GetPluginNames()) {
					if (first) {
						sender.sendMessage("§6---- Plugin names ----");
						first = false;
					}
					sender.sendMessage("- " + plugin2);
				}
			} else {
				TBMCCoreAPI.UpdatePlugin(plugin, sender, branch == null ? "master" : branch);
			}
		});
	}

	@Override
	public String[] getHelpText(Method method, Command2.Subcommand ann) {
		return new String[]{ //
			"§6---- Update plugin ----", //
			"This command downloads the latest version of a custom plugin from GitHub", //
			"To update a plugin: add its name", //
			"To list the plugin names: don't type a name" //
		};
	}
}
