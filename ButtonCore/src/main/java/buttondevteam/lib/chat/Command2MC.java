package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;

import java.util.HashMap;
import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> {
	private HashMap<String, SubcommandData<ICommand2MC>> subcommands = new HashMap<>();
	private HashMap<Class<?>, ParamConverter<?>> paramConverters = new HashMap<>();

	@Override
	public boolean handleCommand(Command2MCSender sender, String commandLine) throws Exception {
		return handleCommand(sender, commandLine, subcommands, paramConverters);
	}

	@Override
	public void registerCommand(ICommand2MC command) {
		registerCommand(command, subcommands, '/');
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC command) {
		return MainPlugin.permission.has(sender.getSender(), "thorpe.command." + command.getCommandPath().replace(' ', '.'));
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
