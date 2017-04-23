package buttondevteam.lib.chat;

import java.util.function.Function;

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
		Function<Class<?>, String> getFromClass = cl -> getClass().getSimpleName().toLowerCase()
				.replace("commandbase", "").replace("command", "");
		String path = getClass().getAnnotation(CommandClass.class).path(), prevpath = path; // TODO: Check if annotation exists (No @Inherited?)
		for (Class<?> cl = getClass().getSuperclass(); cl != null
				&& !cl.getName().equals(TBMCCommandBase.class.getName()); cl = cl.getSuperclass()) {
			//com.sun.xml.internal.bind.v2.TODO.prototype();
			String newpath = cl.getAnnotation(CommandClass.class).path();
			if (newpath.length() == 0)
				newpath = getFromClass.apply(cl);
			if (!newpath.equals(prevpath))
				path = (prevpath = newpath) + " " + path;
		}
		return path.length() == 0 ? getFromClass.apply(getClass()) : path;
	}

	Plugin plugin; // Used By TBMCChatAPI

	public final Plugin getPlugin() { // Used by CommandCaller (ButtonChat)
		return plugin;
	}

	public final boolean isPlayerOnly() {
		return this instanceof PlayerCommandBase ? true
				: this instanceof OptionallyPlayerCommandBase
						? getClass().isAnnotationPresent(OptionallyPlayerCommandClass.class)
								? getClass().getAnnotation(OptionallyPlayerCommandClass.class).playerOnly() : true
						: false;
	}
}
