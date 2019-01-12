package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class Command2 {
	/**
	 * Default handler for commands, can be used to copy the args too.
	 *
	 * @param sender  The sender which ran the command
	 * @param command The (sub)command ran by the user
	 * @param args    All of the arguments passed as is
	 * @return
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
}
