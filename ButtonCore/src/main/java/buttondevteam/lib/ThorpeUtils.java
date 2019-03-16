package buttondevteam.lib;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ThorpeUtils {
	private ThorpeUtils() {}

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
		 * @return The full name that can be used to uniquely indentify the user.
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
}
