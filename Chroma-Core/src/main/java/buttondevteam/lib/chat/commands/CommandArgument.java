package buttondevteam.lib.chat.commands;

import lombok.RequiredArgsConstructor;

/**
 * A command argument's information to be used to construct the command.
 */
@RequiredArgsConstructor
public class CommandArgument {
	public final String name;
	public final Class<?> type;
	public final String description;
}
