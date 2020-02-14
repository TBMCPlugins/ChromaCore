package buttondevteam.lib.chat;

import buttondevteam.lib.architecture.ButtonPlugin;

public abstract class ICommand2MC<T extends ButtonPlugin<T>> extends ICommand2<Command2MCSender> {
	public ICommand2MC() {
	}

	public void onRegister(Command2MC<T> manager) {
	}

	@Override
	public <TX extends ICommand2<Command2MCSender>> void onRegister(Command2<TX, Command2MCSender> manager) {
		super.onRegister(manager);
		onRegister((Command2MC<T>) manager);
	}
}
