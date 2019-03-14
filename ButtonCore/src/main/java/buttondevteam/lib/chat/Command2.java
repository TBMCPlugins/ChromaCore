package buttondevteam.lib.chat;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import lombok.RequiredArgsConstructor;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

/**
 * The method name is the subcommand, use underlines (_) to add further subcommands.
 * The args may be null if the conversion failed and it's optional.
 */
public abstract class Command2<TC extends ICommand2, TP extends Command2Sender> {
	protected Command2() {
		commandHelp.add("§6---- Commands ----");
	}

	/**
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
		/**
		 * Help text to show players. A usage message will be also shown below it.
		 */
		String[] helpText() default {};
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface OptionalArg {
	}

	@RequiredArgsConstructor
	protected static class SubcommandData<T extends ICommand2> {
		public final Method method;
		public final T command;
		public final String[] helpText;
	}

	@RequiredArgsConstructor
	protected static class ParamConverter<T> {
		public final Function<String, T> converter;
		public final String errormsg;
	}

	private HashMap<String, SubcommandData<TC>> subcommands = new HashMap<>();
	private HashMap<Class<?>, ParamConverter<?>> paramConverters = new HashMap<>();

	private ArrayList<String> commandHelp = new ArrayList<>(); //Mainly needed by Discord

	/**
	 * Adds a param converter that obtains a specific object from a string parameter.
	 * The converter may return null.
	 *
	 * @param cl        The class of the result object
	 * @param converter The converter to use
	 * @param <T>       The type of the result
	 */
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg) {
		paramConverters.put(cl, new ParamConverter<>(converter, errormsg));
	}

	public boolean handleCommand(TP sender, String commandline) throws Exception {
		for (int i = commandline.length(); i != -1; i = commandline.lastIndexOf(' ', i - 1)) {
			String subcommand = commandline.substring(0, i).toLowerCase();
			SubcommandData<TC> sd = subcommands.get(subcommand); //O(1)
			if (sd == null) continue;
			if (sd.method == null || sd.command == null) { //Main command not registered, but we have subcommands
				sender.sendMessage(sd.helpText);
				return true;
			}
			if (!hasPermission(sender, sd.command)) {
				sender.sendMessage("§cYou don't have permission to use this command");
				return true;
			}
			val params = new ArrayList<Object>(sd.method.getParameterCount());
			int j = subcommand.length(), pj;
			Class<?>[] parameterTypes = sd.method.getParameterTypes();
			if (parameterTypes.length == 0)
				throw new Exception("No sender parameter for method '" + sd.method + "'");
			val sendertype = parameterTypes[0];
			final ChromaGamerBase cg;
			if (sendertype.isAssignableFrom(sender.getClass()))
				params.add(sender); //The command either expects a CommandSender or it is a Player, or some other expected type
			else if (sender instanceof Command2MCSender
				&& sendertype.isAssignableFrom(((Command2MCSender) sender).getSender().getClass()))
				params.add(((Command2MCSender) sender).getSender());
			else if (ChromaGamerBase.class.isAssignableFrom(sendertype)
				&& sender instanceof Command2MCSender
				&& (cg = ChromaGamerBase.getFromSender(((Command2MCSender) sender).getSender())) != null
				&& cg.getClass() == sendertype) //The command expects a user of our system
				params.add(cg);
			else {
				sender.sendMessage("§cYou need to be a " + sendertype.getSimpleName() + " to use this command.");
				return true;
			}
			val paramArr = sd.method.getParameters();
			for (int i1 = 1; i1 < parameterTypes.length; i1++) {
				Class<?> cl = parameterTypes[i1];
				pj = j + 1; //Start index
				if (pj == commandline.length() + 1) { //No param given
					if (paramArr[i1].isAnnotationPresent(OptionalArg.class)) {
						params.add(null);
						continue; //Fill the remaining params with nulls
					} else {
						sender.sendMessage(sd.helpText); //Required param missing
						return true;
					}
				}
				j = commandline.indexOf(' ', j + 1); //End index
				if (j == -1 || paramArr[i1].isAnnotationPresent(TextArg.class)) //Last parameter
					j = commandline.length();
				String param = commandline.substring(pj, j);
				if (cl == String.class) {
					params.add(param);
					continue;
				}
				val conv = paramConverters.get(cl);
				if (conv == null)
					throw new Exception("No suitable converter found for parameter type '" + cl.getCanonicalName() + "' for command '" + sd.method.toString() + "'");
				val cparam = conv.converter.apply(param);
				if (cparam == null) {
					sender.sendMessage(conv.errormsg); //Param conversion failed - ex. plugin not found
					return true;
				}
				params.add(cparam);
			}
			//System.out.println("Our params: "+params);
			val ret = sd.method.invoke(sd.command, params.toArray()); //I FORGOT TO TURN IT INTO AN ARRAY (for a long time)
			if (ret instanceof Boolean) {
				if (!(boolean) ret) //Show usage
					sender.sendMessage(sd.helpText);
			} else if (ret != null)
				throw new Exception("Wrong return type! Must return a boolean or void. Return value: "+ret);
			return true; //We found a method
		}
		return false; //Didn't handle
	} //TODO: Add to the help

	public abstract void registerCommand(TC command);

	protected void registerCommand(TC command, char commandChar) {
		val path = command.getCommandPath();
		int x = path.indexOf(' ');
		val mainPath = commandChar + path.substring(0, x == -1 ? path.length() : x);
		//var scmdmap = subcommandStrings.computeIfAbsent(mainPath, k -> new HashSet<>()); //Used to display subcommands
		val scmdHelpList = new ArrayList<String>();
		Method mainMethod = null;
		boolean nosubs = true;
		try { //Register the default handler first so it can be reliably overwritten
			mainMethod = command.getClass().getMethod("def", Command2Sender.class, String.class);
			val cc = command.getClass().getAnnotation(CommandClass.class);
			var ht = cc == null ? new String[0] : cc.helpText();
			if (ht.length > 0)
				ht[0] = "§6---- " + ht[0] + " ----";
			scmdHelpList.addAll(Arrays.asList(ht));
			scmdHelpList.add("§6Subcommands:");
			if (!commandHelp.contains(mainPath))
				commandHelp.add(mainPath);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Could not register default handler for command /" + path, e);
		}
		for (val method : command.getClass().getMethods()) {
			val ann = method.getAnnotation(Subcommand.class);
			if (ann == null) continue; //Don't call the method on non-subcommands because they're not in the yaml
			var ht = command.getHelpText(method, ann);
			if (ht != null) {
				val subcommand = commandChar + path + //Add command path (class name by default)
					(method.getName().equals("def") ? "" : " " + method.getName().replace('_', ' ').toLowerCase()); //Add method name, unless it's 'def'
				ht = getHelpText(method, ht, subcommand);
				subcommands.put(subcommand, new SubcommandData<>(method, command, ht)); //Result of the above (def) is that it will show the help text
				scmdHelpList.add(subcommand);
				nosubs = false;
			}
		}
		if (nosubs && scmdHelpList.size() > 0)
			scmdHelpList.remove(scmdHelpList.size() - 1); //Remove Subcommands header
		if (mainMethod != null && !subcommands.containsKey(commandChar + path)) //Command specified by the class
			subcommands.put(commandChar + path, new SubcommandData<>(mainMethod, command, scmdHelpList.toArray(new String[0])));
		if (mainMethod != null && !subcommands.containsKey(mainPath)) //Main command, typically the same as the above
			subcommands.put(mainPath, new SubcommandData<>(null, null, scmdHelpList.toArray(new String[0])));
	}

	private String[] getHelpText(Method method, String[] ht, String subcommand) {
		val str = method.getDeclaringClass().getResourceAsStream("/commands.yml");
		if (str == null)
			TBMCCoreAPI.SendException("Error while getting command data!", new Exception("Resource not found!"));
		else {
			if (ht.length > 0)
				ht[0] = "§6---- " + ht[0] + " ----";
			YamlConfiguration yc = YamlConfiguration.loadConfiguration(new InputStreamReader(str)); //Generated by ButtonProcessor
			val ccs = yc.getConfigurationSection(method.getDeclaringClass().getCanonicalName());
			if (ccs != null) {
				val cs = ccs.getConfigurationSection(method.getName());
				if (cs != null) {
					val mname = cs.getString("method");
					val params = cs.getString("params");
					//val goodname = method.getName() + "(" + Arrays.stream(method.getGenericParameterTypes()).map(cl -> cl.getTypeName()).collect(Collectors.joining(",")) + ")";
					int i = mname.indexOf('('); //Check only the name - the whole method is still stored for backwards compatibility and in case it may be useful
					if (i != -1 && method.getName().equals(mname.substring(0, i)) && params != null) {
						String[] both = Arrays.copyOf(ht, ht.length + 1);
						both[ht.length] = "§6Usage:§r " + subcommand + " " + params;
						ht = both;
					} else
						TBMCCoreAPI.SendException("Error while getting command data for " + method + "!", new Exception("Method '" + method.toString() + "' != " + mname + " or params is " + params));
				} else
					TBMCCoreAPI.SendException("Error while getting command data for " + method + "!", new Exception("cs is " + cs));
			} else
				TBMCCoreAPI.SendException("Error while getting command data for " + method + "!", new Exception("ccs is " + ccs + " - class: " + method.getDeclaringClass().getCanonicalName()));
		}
		return ht;
	}

	public abstract boolean hasPermission(TP sender, TC command);

	public String[] getCommandsText() {
		return commandHelp.toArray(new String[0]);
	}
}
