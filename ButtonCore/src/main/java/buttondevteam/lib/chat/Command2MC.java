package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC> {
	private HashMap<String, SubcommandData<ICommand2MC>> subcommands = new HashMap<>();
	private HashMap<Class<?>, ParamConverter<?>> paramConverters = new HashMap<>();

	@Override
	public boolean handleCommand(CommandSender sender, String commandLine) throws Exception {
		return handleCommand(sender, commandLine, subcommands, paramConverters);
	}

	@Override
	public void registerCommand(ICommand2MC command) {
		registerCommand(command, subcommands, '/');
	}

	@Override
	public boolean hasPermission(CommandSender sender, ICommand2MC command) {
		return MainPlugin.permission.has(sender, "thorpe.command." + command.getCommandPath().replace(' ', '.'));
	}

	/**
	 * Automatically colors the message red.
	 * {@see super#addParamConverter}
	 */
	@Override
	public <T> void addParamConverter(Class<T> cl, Function<String, T> converter, String errormsg) {
		addParamConverter(cl, converter, "§c" + errormsg, paramConverters);
	}
}
