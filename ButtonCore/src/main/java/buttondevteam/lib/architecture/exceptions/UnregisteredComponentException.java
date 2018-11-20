package buttondevteam.lib.architecture.exceptions;

import buttondevteam.lib.architecture.Component;

public class UnregisteredComponentException extends Throwable {

	public UnregisteredComponentException(Component component) {
		super("The component '" + component.getClass().getSimpleName() + "' isn't registered!");
	}
}
