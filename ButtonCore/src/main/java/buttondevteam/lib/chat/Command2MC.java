package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> {
	@Override
	public void registerCommand(ICommand2MC command) {
		super.registerCommand(command, '/');
		var perm = "thorpe.command." + command.getCommandPath().replace(' ', '.');
		if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
			System.out.println("Adding perm " + perm + " with default: "
				+ (modOnly(command) ? PermissionDefault.OP : PermissionDefault.TRUE)); //Allow commands by default, unless it's mod only - TODO: Test
		if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
			Bukkit.getPluginManager().addPermission(new Permission(perm,
				modOnly(command) ? PermissionDefault.OP : PermissionDefault.TRUE)); //Allow commands by default, unless it's mod only - TODO: Test
		for (val method : command.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Subcommand.class)) continue;
			String pg = permGroup(command, method);
			if (pg.length() == 0) continue;
			perm = "thorpe." + pg;
			if (Bukkit.getPluginManager().getPermission(perm) == null) //It may occur multiple times
				System.out.println("Adding perm " + perm + " with default: "
					+ PermissionDefault.OP); //Do not allow any commands that belong to a group
			if (Bukkit.getPluginManager().getPermission(perm) == null) //It may occur multiple times
				Bukkit.getPluginManager().addPermission(new Permission(perm,
					//pg.equals(Subcommand.MOD_GROUP) ? PermissionDefault.OP : PermissionDefault.TRUE)); //Allow commands by default, unless it's mod only
					PermissionDefault.OP)); //Do not allow any commands that belong to a group
		}
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC command, Method method) {
		if (sender.getSender() instanceof ConsoleCommandSender) return true; //Always allow the console
		String pg;
		boolean p = true;
		String[] perms = {
			"thorpe.command." + command.getCommandPath().replace(' ', '.'),
			(pg = permGroup(command, method)).length() > 0 ? "thorpe." + pg : null,
			modOnly(command) ? "tbmc.admin" : null
		};
		for (String perm : perms) {
			if (perm != null) {
				if (p) { //Use OfflinePlayer to avoid fetching player data
					if (sender.getSender() instanceof Player)
						p = MainPlugin.permission.playerHas(null, (OfflinePlayer) sender.getSender(), perm);
					else
						//p = MainPlugin.permission.groupHas((String) null, MainPlugin.Instance.unconnPermGroup().get(), perm);
						p = MainPlugin.permission.playerHas(null, Bukkit.getOfflinePlayer(new UUID(0, 0)), perm);
				} else break; //If any of the permissions aren't granted then don't allow
			}
		}
		return p;
	}

	/**
	 * Returns true if this class or <u>any</u> of the superclasses are mod only.
	 *
	 * @param command The command to check
	 * @return Whether the command is mod only
	 */
	private boolean modOnly(ICommand2MC command) {
		return getAnnForValue(command.getClass(), CommandClass.class, CommandClass::modOnly, false);
	}

	/**
	 * Returns true if this class or <u>any</u> of the superclasses are mod only.
	 *
	 * @param method The subcommand to check
	 * @return The permission group for the subcommand or empty string
	 */
	private String permGroup(ICommand2MC command, Method method) {
		//System.out.println("Perm group for command " + command.getClass().getSimpleName() + " and method " + method.getName());
		val sc = method.getAnnotation(Subcommand.class);
		if (sc != null && sc.permGroup().length() > 0) {
			//System.out.println("Returning sc.permGroup(): " + sc.permGroup());
			return sc.permGroup();
		}
		//System.out.println("Returning getAnnForValue(" + command.getClass().getSimpleName() + ", ...): " + getAnnForValue(command.getClass(), CommandClass.class, CommandClass::permGroup, null));
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
}
