package buttondevteam.lib;

import buttondevteam.core.MainPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.function.Supplier;

public final class ChromaUtils {
	private ChromaUtils() {}

	public static String getDisplayName(CommandSender sender) {
		if (sender instanceof IHaveFancyName)
			return ((IHaveFancyName) sender).getFancyName();
		if (sender instanceof Player)
			return ((Player) sender).getDisplayName();
		return sender.getName();
	}

	public static String getFullDisplayName(CommandSender sender) {
		if (sender instanceof IHaveFancyName)
			return ((IHaveFancyName) sender).getFancyFullName();
		return getDisplayName(sender);
	}

	public interface IHaveFancyName {
		/**
		 * May not be null.
		 *
		 * @return The name to be displayed in most places.
		 */
		String getFancyName();

		/**
		 * May return null.
		 *
		 * @return The full name that can be used to uniquely identify the user.
		 */
		String getFancyFullName();
	}

	public static Number convertNumber(Number number, Class<? extends Number> targetcl) {
		if (targetcl == long.class || Long.class.isAssignableFrom(targetcl))
			return number.longValue();
		else if (targetcl == int.class || Integer.class.isAssignableFrom(targetcl))
			return number.intValue(); //Needed because the parser can get longs
		else if (targetcl == short.class || Short.class.isAssignableFrom(targetcl))
			return number.shortValue();
		else if (targetcl == byte.class || Byte.class.isAssignableFrom(targetcl))
			return number.byteValue();
		else if (targetcl == float.class || Float.class.isAssignableFrom(targetcl))
			return number.floatValue();
		else if (targetcl == double.class || Double.class.isAssignableFrom(targetcl))
			return number.doubleValue();
		return number;
	}

	/**
	 * Calls the event always asynchronously. The return value is always false if async.
	 *
	 * @param event The event to call
	 * @return The event cancelled state or false if async.
	 */
	public static <T extends Event & Cancellable> boolean callEventAsync(T event) {
		Supplier<Boolean> task = () -> {
			Bukkit.getPluginManager().callEvent(event);
			return event.isCancelled();
		};
		return doItAsync(task, false);
	}

	/**
	 * Does something always asynchronously. It will execute in the same thread if it's not the server thread.
	 *
	 * @param what What to do
	 * @param def  Default if async
	 * @return The value supplied by the action or def if async.
	 */
	public static <T> T doItAsync(Supplier<T> what, T def) {
		if (Bukkit.isPrimaryThread())
			Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, what::get);
		else
			return what.get();
		return def;
	}

	private static boolean test = false;

	/**
	 * Returns true while unit testing.
	 */
	public static boolean isTest() { return test; }

	/**
	 * Call when unit testing.
	 */
	public static void setTest() { test = true; }
}
