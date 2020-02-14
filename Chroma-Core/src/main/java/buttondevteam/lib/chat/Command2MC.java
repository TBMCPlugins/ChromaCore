package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.ButtonPlugin;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class Command2MC<TP extends ButtonPlugin<TP>> extends Command2<ICommand2MC<TP>, Command2MCSender> implements Listener {
	@Override
	public void registerCommand(ICommand2MC<TP> command) {
		super.registerCommand(command, '/');
		var perm = "chroma.command." + command.getCommandPath().replace(' ', '.');
		if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
			Bukkit.getPluginManager().addPermission(new Permission(perm,
				PermissionDefault.TRUE)); //Allow commands by default, it will check mod-only
		for (val method : command.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Subcommand.class)) continue;
			var path = getCommandPath(method.getName(), '.');
			if (path.length() > 0) {
				var subperm = perm + path;
				if (Bukkit.getPluginManager().getPermission(subperm) == null) //Check needed for plugin reset
					Bukkit.getPluginManager().addPermission(new Permission(subperm,
						PermissionDefault.TRUE)); //Allow commands by default, it will check mod-only
			}
			String pg = permGroup(command, method);
			if (pg.length() == 0) continue;
			perm = "chroma." + pg;
			if (Bukkit.getPluginManager().getPermission(perm) == null) //It may occur multiple times
				Bukkit.getPluginManager().addPermission(new Permission(perm,
					PermissionDefault.OP)); //Do not allow any commands that belong to a group
		}
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC<TP> command, Method method) {
		return hasPermission(sender.getSender(), command, method);
	}

	public boolean hasPermission(CommandSender sender, ICommand2MC<TP> command, Method method) {
		if (sender instanceof ConsoleCommandSender) return true; //Always allow the console
		String pg;
		boolean p = true;
		String[] perms = {
			"chroma.command." + command.getCommandPath().replace(' ', '.'),
			(pg = permGroup(command, method)).length() > 0 ? "chroma." + pg : null
		};
		for (String perm : perms) {
			if (perm != null) {
				if (p) { //Use OfflinePlayer to avoid fetching player data
					if (sender instanceof OfflinePlayer)
						p = MainPlugin.permission.playerHas(sender instanceof Player ? ((Player) sender).getLocation().getWorld().getName() : null, (OfflinePlayer) sender, perm);
					else
						p = false; //Use sender's method
					//System.out.println("playerHas " + perm + ": " + p);
					//System.out.println("hasPermission: " + sender.hasPermission(perm));
					if (!p) p = sender.hasPermission(perm);
				} else break; //If any of the permissions aren't granted then don't allow
			}
		}
		return p;
	}

	/**
	 * Returns the first group found in the hierarchy starting from the command method <b>or</b> the mod group if <i>any</i></i> of the superclasses are mod only.
	 *
	 * @param method The subcommand to check
	 * @return The permission group for the subcommand or empty string
	 */
	private String permGroup(ICommand2MC<TP> command, Method method) {
		val sc = method.getAnnotation(Subcommand.class);
		if (sc != null && sc.permGroup().length() > 0) {
			return sc.permGroup();
		}
		if (getAnnForValue(command.getClass(), CommandClass.class, CommandClass::modOnly, false))
			return Subcommand.MOD_GROUP;
		return getAnnForValue(command.getClass(), CommandClass.class, CommandClass::permGroup, "");
	}

	/**
	 * Loops until it finds a value that is <b>not</b> the same as def
	 *
	 * @param sourceCl  The class which has the annotation
	 * @param annCl     The annotation to get
	 * @param annMethod The annotation method to check
	 * @param def       The value to ignore when looking for the result
	 * @param <T>       The annotation type
	 * @param <V>       The type of the value
	 * @return The value returned by the first superclass or def
	 */
	private <T extends Annotation, V> V getAnnForValue(Class<?> sourceCl, Class<T> annCl, Function<T, V> annMethod, V def) {
		for (Class<?> cl = sourceCl; cl != null; cl = cl.getSuperclass()) {
			val cc = cl.getAnnotation(annCl);
			V r;
			if (cc != null && (r = annMethod.apply(cc)) != def) return r;
		}
		return def;
	}

	/**
	 * Automatically colors the message red.
	 * {@see super#addParamConverter}
	 */
	@Override
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg) {
		super.addParamConverter(cl, converter, "§c" + errormsg);
	}

	@EventHandler
	private void handleTabComplete(TabCompleteEvent event) {
		String commandline = event.getBuffer();
		CommandSender sender = event.getSender();
		//System.out.println("tab");
		for (int i = commandline.length(); i != -1; i = commandline.lastIndexOf(' ', i - 1)) {
			String subcommand = commandline.substring(0, i).toLowerCase();
			if (subcommand.length() == 0 || subcommand.charAt(0) != '/') subcommand = '/' + subcommand; //Console
			//System.out.println("Subcommand: " + subcommand);
			SubcommandData<ICommand2MC<TP>> sd = subcommands.get(subcommand); //O(1)
			if (sd == null) continue;
			//System.out.println("ht: " + Arrays.toString(sd.helpText));
			Arrays.stream(sd.helpText).skip(1).map(ht -> new HashMap.SimpleEntry<>(ht, subcommands.get(ht))).filter(e -> e.getValue() != null)
				.filter(kv -> kv.getKey().startsWith(commandline))
				.filter(kv -> hasPermission(sender, kv.getValue().command, kv.getValue().method))
				.forEach(kv -> event.getCompletions().add((kv.getKey()).substring(kv.getKey().lastIndexOf(' ', commandline.length()) + 1)));
			if (sd.method == null || sd.command == null)
				return;
			/*if (!hasPermission(sender, sd.command, sd.method)) { - TODO: Arguments
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
						if (cl.isPrimitive())
							params.add(Defaults.defaultValue(cl));
						else if (Number.class.isAssignableFrom(cl)
							|| Number.class.isAssignableFrom(cl))
							params.add(Defaults.defaultValue(Primitives.unwrap(cl)));
						else
							params.add(null);
						continue; //Fill the remaining params with nulls
					} else {
						sender.sendMessage(sd.helpText); //Required param missing
						return true;
					}
				}
				if (paramArr[i1].isVarArgs()) {
					par0ams.add(commandline.substring(j + 1).split(" +"));
					continue;
				}
				j = commandline.indexOf(' ', j + 1); //End index
				if (j == -1 || paramArr[i1].isAnnotationPresent(TextArg.class)) //Last parameter
					j = commandline.length();
				String param = commandline.substring(pj, j);
				if (cl == String.class) {
					params.add(param);
					continue;
				} else if (Number.class.isAssignableFrom(cl) || cl.isPrimitive()) {
					try {
						//noinspection unchecked
						Number n = ThorpeUtils.convertNumber(NumberFormat.getInstance().parse(param), (Class<? extends Number>) cl);
						params.add(n);
					} catch (ParseException e) {
						sender.sendMessage("§c'" + param + "' is not a number.");
						return true;
					}
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
			try {
				val ret = sd.method.invoke(sd.command, params.toArray()); //I FORGOT TO TURN IT INTO AN ARRAY (for a long time)
				if (ret instanceof Boolean) {
					if (!(boolean) ret) //Show usage
						sender.sendMessage(sd.helpText);
				} else if (ret != null)
					throw new Exception("Wrong return type! Must return a boolean or void. Return value: " + ret);
				return true; //We found a method
			} catch (InvocationTargetException e) {
				TBMCCoreAPI.SendException("An error occurred in a command handler!", e.getCause());
			}*/
		}
	}
}
