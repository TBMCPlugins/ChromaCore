package buttondevteam.lib.chat;

import buttondevteam.lib.architecture.ButtonPlugin;

public abstract class ICommand2MC extends ICommand2<Command2MCSender> {
	public ICommand2MC() {
		super(ButtonPlugin.getCommand2MC());
	}
}
