package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> {
	@Override
	public void registerCommand(ICommand2MC command) {
		super.registerCommand(command, '/');
		val perm = "thorpe.command." + command.getCommandPath().replace(' ', '.');
		if (Bukkit.getPluginManager().getPermission(perm) == null) //Check needed for plugin reset
			Bukkit.getPluginManager().addPermission(new Permission(perm,
				modOnly(command) ? PermissionDefault.OP : PermissionDefault.TRUE)); //Allow commands by default, unless it's mod only - TODO: Test
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC command, Method method) {
		String pg;
		return modOnly(command)
			? MainPlugin.permission.has(sender.getSender(), "tbmc.admin")
			: (pg = permGroup(command, method)) != null
			? MainPlugin.permission.has(sender.getSender(), pg)
			: MainPlugin.permission.has(sender.getSender(), "thorpe.command." + command.getCommandPath().replace(' ', '.'));
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
	 * @return The permission group for the subcommand or null
	 */
	private String permGroup(ICommand2MC command, Method method) {
		val sc = method.getAnnotation(Subcommand.class);
		if (sc != null && sc.permGroup().length() > 0) return sc.permGroup();
		return getAnnForValue(command.getClass(), CommandClass.class, CommandClass::permGroup, null);
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
