package buttondevteam.lib.chat.commands;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandArgument {
	public final String name;
	public final Class<?> type;
	public final String description;
}
