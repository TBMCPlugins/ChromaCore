package buttondevteam.lib.chat;

import lombok.Getter;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

/**
 * This class is used as a base class for all the specific command implementations.
 * It primarily holds information about the command itself and how it should be run, ideally in a programmer-friendly way.
 * Any inferred and processed information about this command will be stored in the command manager (Command2*).
 *
 * @param <TP> The sender's type
 */
public abstract class ICommand2<TP extends Command2Sender> {
	/**
	 * Default handler for commands, can be used to copy the args too.
	 *
	 * @param sender The sender which ran the command
	 * @return The success of the command
	 */
	@SuppressWarnings("unused")
	public boolean def(TP sender) {
		return false;
	}

	/**
	 * Convenience method. Return with this.
	 *
	 * @param sender  The sender of the command
	 * @param message The message to send to the sender
	 * @return Always true so that the usage isn't shown
	 */
	protected boolean respond(TP sender, String message) {
		sender.sendMessage(message);
		return true;
	}

	/**
	 * Return null to not add any help text, return an empty array to only print subcommands.<br>
	 * By default, returns null if the Subcommand annotation is not present and returns an empty array if no help text can be found.
	 *
	 * @param method The method of the subcommand
	 * @return The help text, empty array or null
	 */
	public String[] getHelpText(Method method, Command2.Subcommand ann) {
		val cc = getClass().getAnnotation(CommandClass.class);
		return ann.helpText().length != 0 || cc == null ? ann.helpText() : cc.helpText(); //If cc is null then it's empty array
	}

	private final String path;
	@Getter
	private final Command2<?, TP> manager; //TIL that if I use a raw type on a variable then none of the type args will work (including what's defined on a method, not on the type)

	public <T extends ICommand2<TP>> ICommand2(Command2<T, TP> manager) {
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
	public String getCommandPath() {
		return path;
	}

	private static final String[] EMPTY_PATHS = new String[0];

	/**
	 * All of the command's paths it will be invoked on. Does not include aliases or the default path.
	 * Must be lowercase and must include the full path.
	 *
	 * @return The full command paths that this command should be registered under in addition to the default one.
	 */
	public String[] getCommandPaths() { // TODO: Deal with this (used for channel IDs)
		return EMPTY_PATHS;
	}

	private String getcmdpath() {
		if (!getClass().isAnnotationPresent(CommandClass.class))
			throw new RuntimeException(
				"No @CommandClass annotation on command class " + getClass().getSimpleName() + "!");
		Function<Class<?>, String> getFromClass = cl -> cl.getSimpleName().toLowerCase().replace("commandbase", "") // <-- ...
			.replace("command", "");
		String path = getClass().getAnnotation(CommandClass.class).path(),
			prevpath = path = path.length() == 0 ? getFromClass.apply(getClass()) : path;
		for (Class<?> cl = getClass().getSuperclass(); cl != null
			&& !cl.getPackage().getName().equals(ICommand2MC.class.getPackage().getName()); cl = cl
			.getSuperclass()) { //
			String newpath;
			if (!cl.isAnnotationPresent(CommandClass.class)
				|| (newpath = cl.getAnnotation(CommandClass.class).path()).length() == 0
				|| newpath.equals(prevpath)) {
				if ((Modifier.isAbstract(cl.getModifiers()) && !cl.isAnnotationPresent(CommandClass.class))
					|| cl.getAnnotation(CommandClass.class).excludeFromPath()) // <--
					continue;
				newpath = getFromClass.apply(cl);
			}
			path = (prevpath = newpath) + " " + path;
		}
		return path;
	}
}
