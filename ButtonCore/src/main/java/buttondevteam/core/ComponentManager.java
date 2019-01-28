package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import lombok.val;

public final class ComponentManager {
	private ComponentManager() {}

	private static boolean componentsEnabled = false;

	/**
	 * This flag is used to enable components registered after the others were enabled.
	 * @return Whether already registered components have been enabled
	 */
	public static boolean areComponentsEnabled() { return componentsEnabled; }

	/**
	 * Enables components based on a configuration - any component registered afterwards will be also enabled
	 */
	public static void enableComponents() {
		//Component.getComponents().values().stream().filter(c->cs.getConfigurationSection(c.getClass().getSimpleName()).getBoolean("enabled")).forEach(c-> {
		Component.getComponents().values().stream().filter(c -> c.shouldBeEnabled().get()).forEach(c -> {
			try {
				Component.setComponentEnabled(c, true);
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Failed to enable one of the components: " + c.getClass().getSimpleName(), e);
			}
		});
		componentsEnabled = true;
	}

	/**
	 * Disables all components that are enabled
	 */
	public static void disableComponents() {
		Component.getComponents().values().stream().filter(Component::isEnabled).forEach(c -> {
			try {
				Component.setComponentEnabled(c, false);
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Failed to disable one of the components: " + c.getClass().getSimpleName(), e);
			}
		});
		componentsEnabled = false;
	}

	/**
	 * Will also return false if the component is not registered.
	 *
	 * @param cl The component class
	 * @return Whether the component is registered and enabled
	 */
	public static boolean isEnabled(Class<? extends Component> cl) {
		val c = Component.getComponents().get(cl);
		return c != null && c.isEnabled();
	}

	/**
	 * Will also return false if the component is not registered.
	 *
	 * @param cl The component class
	 * @return The component if it's registered and enabled
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Component> T getIfEnabled(Class<T> cl) {
		val c = Component.getComponents().get(cl);
		return c != null && c.isEnabled() ? (T) c : null;
	}
}
