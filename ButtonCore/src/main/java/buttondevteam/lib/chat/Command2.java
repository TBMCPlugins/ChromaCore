package buttondevteam.lib.chat;

import buttondevteam.lib.player.ChromaGamerBase;
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
	 * @param args    All of the arguments passed as is
	 * @return The success of the command
	 */
	@Subcommand
	public boolean def(CommandSender sender, @TextArg String args) {
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
		for (int i = commandline.length(); i != -1; i = commandline.lastIndexOf(' ', i - 1)) {
			String subcommand = commandline.substring(0, i).toLowerCase();
			//System.out.println("Subcommand: "+subcommand);
			//System.out.println("Subcmds: "+subcommands.toString());
			SubcommandData sd = subcommands.get(subcommand); //O(1)
			if (sd == null) continue; //TODO: This will run each time someone runs any command
			//System.out.println("sd.method: "+sd.method); //TODO: Rename in Maven
			val params = new ArrayList<Object>(sd.method.getParameterCount());
			int j = subcommand.length(), pj;
			Class<?>[] parameterTypes = sd.method.getParameterTypes();
			if (parameterTypes.length == 0)
				throw new Exception("No sender parameter for method '" + sd.method + "'");
			val sendertype = parameterTypes[0];
			final ChromaGamerBase cg;
			if (sendertype.isAssignableFrom(sender.getClass()))
				params.add(sender); //The command either expects a CommandSender or it is a Player, or some other expected type
			else if (ChromaGamerBase.class.isAssignableFrom(sendertype)
				&& (cg = ChromaGamerBase.getFromSender(sender)) != null
				&& cg.getClass() == sendertype) //The command expects a user of our system
				params.add(cg);
			else {
				sender.sendMessage("§cYou need to be a " + sendertype.getSimpleName() + " to use this command.");
				return true;
			}
			for (int i1 = 1; i1 < parameterTypes.length; i1++) {
				Class<?> cl = parameterTypes[i1];
				pj = j + 1; //Start index
				if (pj == commandline.length() + 1) { //No param given
					params.add(null);
					continue; //Fill the remaining params with nulls
				}
				j = commandline.indexOf(' ', j + 1); //End index
				if (j == -1) //Last parameter
					j = commandline.length();
				String param = commandline.substring(pj, j);
				if (cl == String.class) {
					params.add(param);
					continue;
				}
				val conv = paramConverters.get(cl);
				if (conv == null)
					throw new Exception("No suitable converter found for parameter type '" + cl.getCanonicalName() + "' for command '" + sd.method.toString() + "'");
				params.add(conv.apply(param));
			}
			//System.out.println("Our params: "+params);
			val ret = sd.method.invoke(sd.command, params.toArray()); //I FORGOT TO TURN IT INTO AN ARRAY (for a long time)
			if (ret instanceof Boolean) {
				if (!(boolean) ret)
					sender.sendMessage("Wrong usage."); //TODO: Show help text
			} else if (ret != null)
				throw new Exception("Wrong return type! Must return a boolean or void. Return value: "+ret);
			return true; //We found a method
		}
		return false; //Didn't handle
	} //TODO: Add to the help

	public static void registerCommand(Command2 command) {
		for (val method : command.getClass().getMethods())
			if (method.isAnnotationPresent(Subcommand.class))
				subcommands.put("/" + command.path + //Add command path (class name by default)
						(method.getName().equals("def") ? "" : " " + method.getName().replace('_', ' ').toLowerCase()), //Add method name, unless it's 'def'
					new SubcommandData(method, command)); //Result of the above (def) is that it will show the help text
	}

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
} //TODO: Test support of Player instead of CommandSender
