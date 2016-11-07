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

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <br>
	 * <u>Note:</u> If you have a command which has subcommands (like /u admin), you need a separate command class for that as well.
	 * 
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it)
	 */
	
	public abstract String[] GetHelpText(String alias);
	
	public String GetCommandPath() {
		return getClass().getSimpleName().toLowerCase().replace("command", "");
	}

	/**
	 * Determines whether the command can only be used as a player, or command blocks or the console can use it as well.
	 * 
	 * @return If the command is player only
	 */
	public abstract boolean GetPlayerOnly();

	/**
	 * Determines whether the command can only be used by mods or regular players can use it as well.
	 * 
	 * @return If the command is mod only
	 */
	public abstract boolean GetModOnly();

	Plugin plugin; // Used By TBMCChatAPI

	public final Plugin getPlugin() { // Used by CommandCaller (ButtonChat)
		return plugin;
	}
}
