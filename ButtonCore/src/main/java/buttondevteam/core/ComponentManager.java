package buttondevteam.core;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException;

public final class ComponentManager {
	private ComponentManager() {}

	/**
	 * Enables components based on a configuration
	 */
	public static void enableComponents() {
		//Component.getComponents().values().stream().filter(c->cs.getConfigurationSection(c.getClass().getSimpleName()).getBoolean("enabled")).forEach(c-> {
		Component.getComponents().values().stream().filter(c -> c.shouldBeEnabled().get()).forEach(c -> {
			try {
				Component.setComponentEnabled(c, true);
			} catch (UnregisteredComponentException ignored) { //This *should* never happen
			}
		});
	}

	/**
	 * Disables all components that are enabled
	 */
	public static void disableComponents() {
		Component.getComponents().values().stream().filter(Component::isEnabled).forEach(c -> {
			try {
				Component.setComponentEnabled(c, false);
			} catch (UnregisteredComponentException ignored) { //This *should* never happen
			}
		});
	}
}
