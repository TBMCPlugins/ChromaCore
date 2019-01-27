package buttondevteam.lib.chat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * The method name is the subcommand, use underlines (_) to add further subcommands.
 * The args may be null if the conversion failed.
 */
public abstract class Command2 {
	/**
	 * Default handler for commands, can be used to copy the args too.
	 *
	 * @param sender  The sender which ran the command
	 * @param command The (sub)command ran by the user
	 * @param args    All of the arguments passed as is
	 * @return The success of the command
	 */
	public boolean def(CommandSender sender, String command, @TextArg String args) {
		return false;
	}

	/**
	 * TODO: @CommandClass(helpText=...)
	 * Parameters annotated with this receive all of the remaining arguments
	 */
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TextArg {
	}

	/**
	 * Methods annotated with this will be recognised as subcommands
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Subcommand {
	}

	@RequiredArgsConstructor
	private static class SubcommandData {
		public final Method method;
		public final Command2 command;
	}

	private static HashMap<String, SubcommandData> subcommands = new HashMap<>();
	private static HashMap<Class<?>, Function<String, ?>> paramConverters = new HashMap<>();

	public Command2() {
		for (val method : getClass().getMethods())
			if (method.isAnnotationPresent(Subcommand.class))
				subcommands.put(method.getName().replace('_', ' '), new SubcommandData(method, this));
		path = getcmdpath();
	}

	/**
	 * Adds a param converter that obtains a specific object from a string parameter.
	 * The converter may return null.
	 *
	 * @param cl        The class of the result object
	 * @param converter The converter to use
	 * @param <T>       The type of the result
	 */
	public static <T> void addParamConverter(Class<T> cl, Function<String, T> converter) {
		paramConverters.put(cl, converter);
	}

	public static boolean handleCommand(CommandSender sender, String commandline) throws Exception {
		for (int i = commandline.lastIndexOf(' '); i != -1; i = commandline.lastIndexOf(' ', i - 1)) {
			String subcommand = commandline.substring(0, i);
			SubcommandData sd = subcommands.get(subcommand); //O(1)
			if (sd == null) continue; //TODO: This will run each time someone runs any command
			val params = new ArrayList<Object>(sd.method.getParameterCount());
			int j = 0, pj;
			for (val cl : sd.method.getParameterTypes()) {
				pj = j + 1;
				j = commandline.indexOf(' ', j + 1);
				String param = subcommand.substring(pj, j);
				if (cl == String.class) {
					params.add(param);
					continue;
				}
				val conv = paramConverters.get(cl);
				if (conv == null)
					throw new Exception("No suitable converter found for parameter type '" + cl.getCanonicalName() + "' for command '" + sd.method.toString() + "'");
				params.add(conv.apply(param));
			}
			sd.method.invoke(sd.command, params);
			return true; //We found a method
		}
		return false; //Didn't handle
	} //TODO: Use preprocess event and add to the help

	private final String path;

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
} //TODO: Support Player instead of CommandSender
