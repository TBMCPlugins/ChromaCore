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
}
