package buttondevteam.lib.chat;

import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import lombok.Getter;

import javax.annotation.Nullable;

@SuppressWarnings("JavadocReference")
public abstract class ICommand2MC extends ICommand2<Command2MCSender> {
	@Getter
	private ButtonPlugin plugin;
	@Getter
	@Nullable
	private Component<?> component;

	public ICommand2MC() {
		super(ButtonPlugin.getCommand2MC());
	}

	/**
	 * Called from {@link buttondevteam.lib.architecture.Component#registerCommand(ICommand2MC)} and {@link ButtonPlugin#registerCommand(ICommand2MC)}
	 */
	public void registerToPlugin(ButtonPlugin plugin) {
		if (this.plugin == null)
			this.plugin = plugin;
		else
			throw new IllegalStateException("The command is already assigned to a plugin!");
	}

	/**
	 * Called from {@link buttondevteam.lib.architecture.Component#registerCommand(ICommand2MC)}
	 */
	public void registerToComponent(Component<?> component) {
		if (this.component == null)
			this.component = component;
		else
			throw new IllegalStateException("The command is already assigned to a component!");
	}

	/*@Override
	public <TX extends ICommand2<Command2MCSender>> void onRegister(Command2<TX, Command2MCSender> manager) {
		super.onRegister(manager);
		onRegister((Command2MC) manager); //If ICommand2 is inherited with the same type arg, this would fail but I don't want to add another type param to ICommand2
	} //For example: class IOffender extends ICommand2<Command2MCSender>*/
}
