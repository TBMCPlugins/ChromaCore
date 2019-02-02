package buttondevteam.lib.chat;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.function.Function;

public abstract class ICommand2 {
	/**
	 * Default handler for commands, can be used to copy the args too.
	 *
	 * @param sender The sender which ran the command
	 * @param args   All of the arguments passed as is
	 * @return The success of the command
	 */
	public boolean def(CommandSender sender, @Command2.TextArg String args) {
		return false;
	}

	/**
	 * Convenience method. Return with this.
	 *
	 * @param sender  The sender of the command
	 * @param message The message to send to the sender
	 * @return Always true so that the usage isn't shown
	 */
	protected boolean respond(CommandSender sender, String message) {
		sender.sendMessage(message);
		return true;
	}

	private final String path;
	@Getter
	private final Command2 manager;

	public ICommand2(Command2 manager) {
		path = getcmdpath();
		this.manager = manager;
	}

	/**
	 * The command's path, or name if top-level command.<br>
	 * For example:<br>
	 * "u admin updateplugin" or "u" for the top level one<br>
	 * <u>The path must be lowercase!</u><br>
	 *
	 * @return The command path, <i>which is the command class name by default</i> (removing any "command" from it) - Change via the {@link CommandClass} annotation
	 */
	public final String getCommandPath() {
		return path;
	}

	private String getcmdpath() {
		if (!getClass().isAnnotationPresent(CommandClass.class))
			throw new RuntimeException(
				"No @CommandClass annotation on command class " + getClass().getSimpleName() + "!");
		Function<Class<?>, String> getFromClass = cl -> cl.getSimpleName().toLowerCase().replace("commandbase", "") // <-- ...
			.replace("command", "");
		String path = getClass().getAnnotation(CommandClass.class).path();
		path = path.length() == 0 ? getFromClass.apply(getClass()) : path;
		return path;
	}
}
