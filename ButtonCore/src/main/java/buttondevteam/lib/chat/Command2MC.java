package buttondevteam.lib.chat;

import buttondevteam.core.MainPlugin;
import lombok.val;

import java.util.function.Function;

public class Command2MC extends Command2<ICommand2MC, Command2MCSender> {
	@Override
	public void registerCommand(ICommand2MC command) {
		super.registerCommand(command, '/');
	}

	@Override
	public boolean hasPermission(Command2MCSender sender, ICommand2MC command) {
		return modOnly(command)
			? MainPlugin.permission.has(sender.getSender(), "tbmc.admin") //TODO: Change when groups are implemented
			: MainPlugin.permission.has(sender.getSender(), "thorpe.command." + command.getCommandPath().replace(' ', '.'));
	}

	/**
	 * Returns true if this class or <u>any</u> of the superclasses are mod only.
	 *
	 * @param command The command to check
	 * @return Whether the command is mod only
	 */
	private boolean modOnly(ICommand2MC command) {
		for (Class<?> cl = command.getClass(); cl != null; cl = cl.getSuperclass()) {
			val cc = command.getClass().getAnnotation(CommandClass.class);
			if (cc != null && cc.modOnly()) return true;
		}
		return false;
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
