package buttondevteam.core;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;

@CommandClass(modOnly = true)
public class UpdatePluginCommand extends TBMCCommandBase {
	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Downloading plugin names...");
			boolean first = true;
			for (String plugin : TBMCCoreAPI.GetPluginNames()) {
				if (first) {
					sender.sendMessage("§6---- Plugin names ----");
					first = false;
				}
				sender.sendMessage("- " + plugin);
			}
			return true;
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
				TBMCCoreAPI.UpdatePlugin(args[0], sender, args.length == 1 ? "master" : args[1]);
			});
			return true;
		}
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- Update plugin ----", //
				"This command downloads the latest version of a TBMC plugin from GitHub", //
				"To update a plugin: /" + alias + " <plugin>", //
				"To list the plugin names: /" + alias //
		};
	}
}
