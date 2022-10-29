package buttondevteam.lib.chat.commands;

import buttondevteam.lib.chat.ICommand2;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores information about the subcommand that can be used to construct the Brigadier setup and to get information while executing the command.
 *
 * @param <TC> Command class type
 */
@Builder
@RequiredArgsConstructor
public final class SubcommandData<TC extends ICommand2<?>> {
	/**
	 * The type of the sender running the command.
	 * The actual sender type may not be represented by Command2Sender (TP).
	 * In that case it has to match the expected type.
	 */
	public final Class<?> senderType;
	/**
	 * Command arguments collected from the subcommand method.
	 * Used to construct the arguments for Brigadier and to hold extra information.
	 */
	public final Map<String, CommandArgument> arguments;
	/**
	 * The original command class that this data belongs to. If null, that meaans only the help text can be used.
	 */
	@Nullable
	public final TC command;

	/**
	 * Static help text added through annotations. May be overwritten with the getter.
	 */
	private final String[] staticHelpText;
	/**
	 * Custom help text that depends on the context. Overwrites the static one.
	 * The function receives the sender but its type is not guaranteed to match the one at the subcommand.
	 * It will either match or be a Command2Sender, however.
	 */
	private final Function<Object, String[]> helpTextGetter;

	/**
	 * Get help text for this subcommand.
	 *
	 * @param sender The sender running the command
	 * @return Help text shown to the user
	 */
	public String[] getHelpText(Object sender) {
		return staticHelpText == null ? helpTextGetter.apply(sender) : staticHelpText;
	}
}
