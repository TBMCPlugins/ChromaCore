package buttondevteam.core;

import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Optional;

@CommandClass
public class ChromaCommand extends ICommand2MC {
	public ChromaCommand() {
		getManager().addParamConverter(ButtonPlugin.class, name ->
				(ButtonPlugin) Optional.ofNullable(Bukkit.getPluginManager().getPlugin(name))
					.filter(plugin -> plugin instanceof ButtonPlugin).orElse(null),
			"No Chroma plugin found by that name.", () -> Arrays.stream(Bukkit.getPluginManager().getPlugins())
				.filter(plugin -> plugin instanceof ButtonPlugin).map(Plugin::getName)::iterator);
	}

	@Command2.Subcommand
	public void reload(CommandSender sender, @Command2.OptionalArg ButtonPlugin plugin) {
		if (plugin == null)
			plugin = getPlugin();
		if (plugin.tryReloadConfig())
			sender.sendMessage("${ChatColor.AQUA}" + plugin.getName() + " config reloaded.");
		else
			sender.sendMessage("${ChatColor.RED}Failed to reload config. Check console.");
	}

	@Command2.Subcommand
	public void def(CommandSender sender) {
		sender.sendMessage(ButtonPlugin.getCommand2MC().getCommandsText());
	}
}
