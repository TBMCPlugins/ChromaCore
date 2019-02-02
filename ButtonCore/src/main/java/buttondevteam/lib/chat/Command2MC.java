package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.function.Function;

public class Command2MC extends Command2 {

	private HashMap<String, SubcommandData<ICommand2>> subcommands = new HashMap<>();
	private HashMap<Class<?>, Function<String, ?>> paramConverters = new HashMap<>();

	public boolean handleCommand(CommandSender sender, String commandLine) throws Exception {
		return handleCommand(sender, commandLine, subcommands, paramConverters);
	}

	public void registerCommand(ICommand2 command) {
		registerCommand(command, subcommands, '/');
	}

	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter) {
		addParamConverter(cl, converter, paramConverters);
	}
}
