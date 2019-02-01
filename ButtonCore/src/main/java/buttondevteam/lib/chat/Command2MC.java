package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.function.Function;

public class Command2MC extends Command2 {

	private static HashMap<String, SubcommandData<Command2MC>> subcommands = new HashMap<>();
	private static HashMap<Class<?>, Function<String, ?>> paramConverters = new HashMap<>();

	public static boolean handleCommand(CommandSender sender, String commandLine) throws Exception {
		return handleCommand(sender, commandLine, subcommands, paramConverters);
	}

	public static void registerCommand(Command2MC command) {
		registerCommand(command, subcommands, '/');
	}

	public static <T> void addParamConverter(Class<T> cl, Function<String, T> converter) {
		addParamConverter(cl, converter, paramConverters);
	}
}
