package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class CommandCaller implements CommandExecutor {

	private CommandCaller() {
	}

	private static CommandCaller instance;

	public static void RegisterCommand(TBMCCommandBase cmd) throws Exception {
		if (instance == null)
			instance = new CommandCaller();
		String topcmd = cmd.GetCommandPath();
		if (topcmd == null)
			throw new Exception("Command " + cmd.getClass().getSimpleName() + " has no command path!");
		if (cmd.getPlugin() == null)
			throw new Exception("Command " + cmd.GetCommandPath() + " has no plugin!");
		int i;
		if ((i = topcmd.indexOf(' ')) != -1) // Get top-level command
			topcmd = topcmd.substring(0, i);
		{
			PluginCommand pc = ((JavaPlugin) cmd.getPlugin()).getCommand(topcmd);
			if (pc == null)
				throw new Exception("Top level command " + topcmd + " not registered in plugin.yml for plugin: "
						+ cmd.getPlugin().getName());
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
		String[] subcmds = null;
		while (cmd == null && (subcmds = TBMCChatAPI.GetSubCommands(path, sender)).length == 0 && path.contains(" ")) {
			path = path.substring(0, path.lastIndexOf(' '));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path);
		}
		if (cmd == null) {
			if (subcmds == null || subcmds.length > 0)
				sender.sendMessage(subcmds);
			else {
				final String errormsg = "§cYou don't have access to any of this command's subcommands or it doesn't have any.";
				sender.sendMessage(errormsg);
			}
			return true;
		}
		if (cmd.isModOnly() && !MainPlugin.permission.has(sender, "tbmc.admin")) {
			sender.sendMessage("§cYou need to be a mod to use this command.");
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
