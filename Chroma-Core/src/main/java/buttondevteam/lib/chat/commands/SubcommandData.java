package buttondevteam.lib.chat.commands;

import buttondevteam.lib.chat.ICommand2;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public final class SubcommandData<TC extends ICommand2<?>> {
	// The actual sender type may not be represented by Command2Sender (TP)
	public final Class<?> senderType;
	public final Map<String, CommandArgument> arguments;
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

	public String[] getHelpText(Object sender) {
		return staticHelpText == null ? helpTextGetter.apply(sender) : staticHelpText;
	}
}
