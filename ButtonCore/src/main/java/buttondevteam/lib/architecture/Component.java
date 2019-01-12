package buttondevteam.lib.architecture;

import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration is based on class name
 */
public abstract class Component {
	private static HashMap<Class<? extends Component>, Component> components = new HashMap<>();

	@Getter
	private boolean enabled = false;
	@Getter
	@NonNull
	private JavaPlugin plugin;
	@NonNull
	private ConfigurationSection configSect;
	@NonNull
	private @Getter
	IHaveConfig config;

	public final ConfigData<Boolean> shouldBeEnabled() {
		return config.getData("enabled", true);
	}

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.<br>
	 * The component will be enabled automatically, regardless of when it was registered.
	 *
	 * @param component The component to register
	 * @return Whether the component is registered successfully (it may have failed to enable)
	 */
	public static boolean registerComponent(JavaPlugin plugin, Component component) {
		return registerUnregisterComponent(plugin, component, true);
	}

	/**
	 * Unregisters a component by calling {@link #unregister(JavaPlugin)}.<br>
	 * Make sure to unregister the dependencies last.
	 *
	 * @param componentClass The component class to unregister
	 * @return Whether the component is unregistered successfully (it also got disabled)
	 */
	public static boolean unregisterComponent(JavaPlugin plugin, Class<? extends Component> componentClass) {
		val component = components.get(componentClass);
		if (component == null)
			return false; //Failed to load
		return registerUnregisterComponent(plugin, component, false);
	}

	public static boolean registerUnregisterComponent(JavaPlugin plugin, Component component, boolean register) {
		try {
			val metaAnn = component.getClass().getAnnotation(ComponentMetadata.class);
			if (metaAnn != null) {
				Class<? extends Component>[] dependencies = metaAnn.depends();
				for (val dep : dependencies) {
					if (!components.containsKey(dep)) {
						plugin.getLogger().warning("Failed to " + (register ? "" : "un") + "register component " + component.getClassName() + " as a required dependency is missing/disabled: " + dep.getSimpleName());
						return false;
					}
				}
			}
			if (register) {
				component.plugin = plugin;
				if (plugin.getConfig() != null) { //Production
					var compconf = plugin.getConfig().getConfigurationSection("components");
					if (compconf == null) compconf = plugin.getConfig().createSection("components");
					component.configSect = compconf.getConfigurationSection(component.getClassName());
					if (component.configSect == null)
						component.configSect = compconf.createSection(component.getClassName());
					component.config = new IHaveConfig(component.configSect);
				} else //Testing
					component.config = new IHaveConfig(null);
				component.register(plugin);
				components.put(component.getClass(), component);
				if (ComponentManager.areComponentsEnabled() && component.shouldBeEnabled().get()) {
					try { //Enable components registered after the previous ones getting enabled
						setComponentEnabled(component, true);
						return true;
					} catch (Exception | NoClassDefFoundError e) {
						TBMCCoreAPI.SendException("Failed to enable component " + component.getClassName() + "!", e);
						return true;
					}
				}
				return true; //Component shouldn't be enabled
			} else {
				if (component.enabled) {
					try {
						setComponentEnabled(component, false);
					} catch (Exception | NoClassDefFoundError e) {
						TBMCCoreAPI.SendException("Failed to disable component " + component.getClassName() + "!", e);
						return false; //If failed to disable, won't unregister either
					}
				}
				component.unregister(plugin);
				components.remove(component.getClass());
				return true;
			}
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to " + (register ? "" : "un") + "register component " + component.getClassName() + "!", e);
			return false;
		}
	}

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.
	 *
	 * @param component The component to register
	 */
	public static void setComponentEnabled(Component component, boolean enabled) throws UnregisteredComponentException {
		if (!components.containsKey(component.getClass()))
			throw new UnregisteredComponentException(component);
		if (component.enabled == enabled) return; //Don't do anything
		if (component.enabled = enabled)
			component.enable();
		else {
			component.disable();
			component.plugin.saveConfig();
			component.config.resetConfigurationCache();
			TBMCChatAPI.RemoveCommands(component);
		}
	}

	/**
	 * Returns the currently registered components<br>
	 *
	 * @return The currently registered components
	 */
	public static Map<Class<? extends Component>, Component> getComponents() {
		return Collections.unmodifiableMap(components);
	}

	/**
	 * Registers the module, when called by the JavaPlugin class.
	 * This gets fired when the plugin is enabled. Use {@link #enable()} to register commands and such.
	 *
	 * @param plugin Plugin object
	 */
	@SuppressWarnings({"unused", "WeakerAccess"})
	protected void register(JavaPlugin plugin) {
	}

	/**
	 * Unregisters the module, when called by the JavaPlugin class.
	 * This gets fired when the plugin is disabled.
	 * Do any cleanups needed within this method.
	 *
	 * @param plugin Plugin object
	 */
	@SuppressWarnings({"WeakerAccess", "unused"})
	protected void unregister(JavaPlugin plugin) {
	}

	/**
	 * Enables the module, when called by the JavaPlugin class. Call
	 * registerCommand() and registerListener() within this method.<br>
	 * To access the plugin, use {@link #getPlugin()}.
	 */
	protected abstract void enable();

	/**
	 * Disables the module, when called by the JavaPlugin class. Do
	 * any cleanups needed within this method.
	 *     To access the plugin, use {@link #getPlugin()}.
	 */
	protected abstract void disable();

	/**
	 * Registers a TBMCCommand to the component. Make sure to add it to plugin.yml and use {@link buttondevteam.lib.chat.CommandClass}.
	 *
	 * @param commandBase Custom coded command class
	 */
	protected final void registerCommand(TBMCCommandBase commandBase) {
		TBMCChatAPI.AddCommand(this, commandBase);
	}

	/**
	 * Registers a Listener to this component
	 *
	 * @param listener The event listener to register
	 * @return The provided listener
	 */
	protected final Listener registerListener(Listener listener) {
		TBMCCoreAPI.RegisterEventsForExceptions(listener, plugin);
		return listener;
	}

	private String getClassName() {
		return getClass().getSimpleName();
	}
}
