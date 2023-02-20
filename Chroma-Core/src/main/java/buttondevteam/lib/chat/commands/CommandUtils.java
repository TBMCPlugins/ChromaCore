package buttondevteam.lib.chat.commands;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class CommandUtils {
	/**
	 * Returns the path of the given subcommand excluding the class' path. It will start with the given replace char.
	 *
	 * @param methodName  The method's name, method.getName()
	 * @param replaceChar The character to use between subcommands
	 * @return The command path starting with the replacement char.
	 */
	@NotNull
	public static String getCommandPath(String methodName, char replaceChar) {
		return methodName.equals("def") ? "" : replaceChar + methodName.replace('_', replaceChar).toLowerCase();
	}
}
