package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Extend this class to create new TBMCCommand and use {@link TBMCChatAPI#AddCommand(org.bukkit.plugin.java.JavaPlugin, TBMCCommandBase)} to add it. <u><b>Note:</b></u> The command path (command name
 * and subcommand arguments) will be the class name by default, removing any "command" from it. To change it (especially for subcommands), override {@link #GetCommandPath()}.
 * 
 * @author Norbi
 *
 */
public abstract class TBMCCommandBase {

	public TBMCCommandBase() {
	}

	public abstract boolean OnCommand(CommandSender sender, String alias, String[] args);

	public abstract String[] GetHelpText(String alias);

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <u>The path must be lowercase!</u><br>
	 * 
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it)
	 */
	public final String GetCommandPath() {
		if (!getClass().isAnnotationPresent(CommandClass.class))
			throw new RuntimeException("No @Command annotation on command class " + getClass().getSimpleName() + "!");
		String path = getClass().getAnnotation(CommandClass.class).path();
		return path.length() == 0 ? getClass().getSimpleName().toLowerCase().replace("command", "") : path;
	}

	Plugin plugin; // Used By TBMCChatAPI

	public final Plugin getPlugin() { // Used by CommandCaller (ButtonChat)
		return plugin;
	}
}
