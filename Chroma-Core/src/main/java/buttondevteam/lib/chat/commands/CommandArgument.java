package buttondevteam.lib.chat.commands;

import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;

/**
 * A command argument's information to be used to construct the command.
 */
@RequiredArgsConstructor
public class CommandArgument {
	public final String name;
	public final Class<?> type;
	public final boolean greedy;
	public final Pair<Double, Double> limits;
	public final boolean optional;
	public final String description;
}
