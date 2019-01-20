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
		String[] topcmd = new String[1]; //Holds out param
		PluginCommand pc = getPluginCommand(cmd, topcmd);
		pc.setExecutor(instance);
		String[] helptext = cmd.GetHelpText(topcmd[0]);
		if (helptext == null || helptext.length == 0)
			throw new Exception("Command " + cmd.GetCommandPath() + " has no help text!");
		pc.setUsage(helptext.length > 1 ? helptext[1] : helptext[0]);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		StringBuilder path = new StringBuilder(command.getName().toLowerCase());
		for (String arg : args)
			path.append(" ").append(arg);
		TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path.toString());
		int argc = 0;
		String[] subcmds = null;
		while (cmd == null && (subcmds = TBMCChatAPI.GetSubCommands(path.toString(), sender)).length == 0 && path.toString().contains(" ")) {
			path = new StringBuilder(path.substring(0, path.toString().lastIndexOf(' ')));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path.toString());
		}
		if (cmd == null) {
			if (subcmds.length > 0) //Subcmds will always have value here (see assignment above)
				sender.sendMessage(subcmds);
			else {
				final String errormsg = "§cYou don't have access to any of this command's subcommands or it doesn't have any.";
				sender.sendMessage(errormsg);
			}
			return true;
		}
        if (cmd.isModOnly() && (MainPlugin.permission != null ? !MainPlugin.permission.has(sender, "tbmc.admin") : !sender.isOp())) {
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

	public static void UnregisterCommand(TBMCCommandBase cmd) throws Exception {
		PluginCommand pc = getPluginCommand(cmd, null);
		pc.setExecutor(null); //Sets the executor to this plugin
	}

	/**
	 * Gets the plugin command from the TBMC command.
	 *
	 * @param cmd        The TBMC command
	 * @param out_topcmd An array with at least 1 elements or null
	 * @return The Bukkit plugin command - an exception is generated if null
	 * @throws Exception If the command isn't set up properly (or a different error)
	 */
	public static PluginCommand getPluginCommand(TBMCCommandBase cmd, String[] out_topcmd) throws Exception {
		String topcmd = cmd.GetCommandPath();
		if (topcmd == null)
			throw new Exception("Command " + cmd.getClass().getSimpleName() + " has no command path!");
		if (cmd.getPlugin() == null)
			throw new Exception("Command " + cmd.GetCommandPath() + " has no plugin!");
		int i;
		if ((i = topcmd.indexOf(' ')) != -1) // Get top-level command
			topcmd = topcmd.substring(0, i);
		if (out_topcmd != null && out_topcmd.length > 0)
			out_topcmd[0] = topcmd;
		{
			PluginCommand pc = ((JavaPlugin) cmd.getPlugin()).getCommand(topcmd);
			if (pc == null)
				throw new Exception("Top level command " + topcmd + " not registered in plugin.yml for plugin: "
					+ cmd.getPlugin().getName());
			return pc;
		}
	}
}
