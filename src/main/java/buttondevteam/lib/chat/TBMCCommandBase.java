package buttondevteam.lib.chat;

import java.util.function.Function;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javassist.Modifier;

/**
 * Extend this class to create new TBMCCommand and use {@link TBMCChatAPI#AddCommand(org.bukkit.plugin.java.JavaPlugin, TBMCCommandBase)} to add it. <u><b>Note:</b></u> The command path (command name
 * and subcommand arguments) will be the class name by default, removing any "command" from it. To change it (especially for subcommands), use the path field in the {@link CommandClass} annotation.
 * 
 * @author Norbi
 *
 */
public abstract class TBMCCommandBase {

	public TBMCCommandBase() {
		path = getcmdpath();
	}

	public abstract boolean OnCommand(CommandSender sender, String alias, String[] args);

	public abstract String[] GetHelpText(String alias);

	private String path = null;

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <u>The path must be lowercase!</u><br>
	 * <b>Abstract classes with no {@link CommandClass} annotations will be ignored.</b>
	 * 
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it) - Change via the {@link CommandClass} annotation
	 */
	public final String GetCommandPath() {
		return path;
	}

	private final String getcmdpath() {
		if (!getClass().isAnnotationPresent(CommandClass.class))
			throw new RuntimeException(
					"No @CommandClass annotation on command class " + getClass().getSimpleName() + "!");
		Function<Class<?>, String> getFromClass = cl -> cl.getSimpleName().toLowerCase().replace("commandbase", "") // <-- ...
				.replace("command", "");
		String path = getClass().getAnnotation(CommandClass.class).path(),
				prevpath = path = path.length() == 0 ? getFromClass.apply(getClass()) : path;
		// System.out.println("Path: " + (path.length() == 0 ? getFromClass.apply(getClass()) : path))
		for (Class<?> cl = getClass().getSuperclass(); cl != null
				&& !cl.getPackage().getName().equals(TBMCCommandBase.class.getPackage().getName()); cl = cl
						.getSuperclass()) { //
			String newpath;
			if (!cl.isAnnotationPresent(CommandClass.class)
					|| (newpath = cl.getAnnotation(CommandClass.class).path()).length() == 0
					|| newpath.equals(prevpath)) {
				if (Modifier.isAbstract(cl.getModifiers()) && (!cl.isAnnotationPresent(CommandClass.class))
						|| cl.getAnnotation(CommandClass.class).excludeFromPath()) // <--
					continue;
				newpath = getFromClass.apply(cl);
			}
			path = (prevpath = newpath) + " " + path;
			// System.out.println("Path: " + (path.length() == 0 ? getFromClass.apply(cl) : path));
		}
		//System.out.println("Path: " + path);
		return path;
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
