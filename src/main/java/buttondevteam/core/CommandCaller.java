package buttondevteam.core;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;

public class CommandCaller implements CommandExecutor {

	private static final String REGISTER_ERROR_MSG = "An error occured while registering commands";

	private CommandCaller() {
	}

	private static CommandCaller instance;

	public static void RegisterCommand(TBMCCommandBase cmd) {
		if (instance == null)
			instance = new CommandCaller();
		if (cmd.GetCommandPath() == null) {
			TBMCCoreAPI.SendException(REGISTER_ERROR_MSG,
					new Exception("Command " + cmd.getClass().getSimpleName() + " has no command path!"));
			return;
		}
		if (cmd.getPlugin() == null) {
			TBMCCoreAPI.SendException(REGISTER_ERROR_MSG,
					new Exception("Command " + cmd.GetCommandPath() + " has no plugin!"));
			return;
		}
		int i;
		String topcmd;
		if ((i = (topcmd = cmd.GetCommandPath()).indexOf(' ')) != -1) // Get top-level command
			topcmd = cmd.GetCommandPath().substring(0, i);
		{
			PluginCommand pc = ((JavaPlugin) cmd.getPlugin()).getCommand(topcmd);
			if (pc == null)
				TBMCCoreAPI.SendException(REGISTER_ERROR_MSG, new Exception("Top level command " + topcmd
						+ " not registered in plugin.yml for plugin: " + cmd.getPlugin().getName()));
			else
				pc.setExecutor(instance);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String path = command.getName().toLowerCase();
		for (String arg : args)
			path += " " + arg;
		TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path);
		int argc = 0;
		while (cmd == null && path.contains(" ")) {
			path = path.substring(0, path.lastIndexOf(' '));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path);
		}
		if (cmd == null) {
			String[] subcmds = TBMCChatAPI.GetSubCommands(path, sender);
			if (subcmds.length > 0)
				sender.sendMessage(subcmds);
			else {
				final String errormsg = "§cYou don't have access to any of this command's subcommands.";
				sender.sendMessage(errormsg);
				if (!(sender instanceof ConsoleCommandSender))
					Bukkit.getConsoleSender().sendMessage(errormsg);
			}
			return true;
		}
		if (cmd.GetModOnly() && !MainPlugin.permission.has(sender, "tbmc.admin")) {
			sender.sendMessage("§cYou need to be a mod to use this command.");
			return true;
		}
		if (cmd.GetPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("§cOnly ingame players can use this command.");
			return true;
		}
		final String[] cmdargs = args.length > 0 ? Arrays.copyOfRange(args, args.length - argc, args.length) : args;
		try {
			if (!cmd.OnCommand(sender, alias, cmdargs)) {
				if (cmd.GetHelpText(alias) == null) {
					sender.sendMessage("Wrong usage, but there's no help text! Error is being reported to devs.");
					throw new NullPointerException("GetHelpText is null for comand /" + cmd.GetCommandPath());
				} else
					sender.sendMessage(cmd.GetHelpText(alias));
			}
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to execute command /" + cmd.GetCommandPath() + " with arguments "
					+ Arrays.toString(cmdargs), e);
		}
		return true;
	}
}
