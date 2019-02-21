package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;

import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> {
	@Override
	public void registerCommand(ICommand2MC command) {
		super.registerCommand(command, '/');
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
		super.addParamConverter(cl, converter, "§c" + errormsg);
	}
}
