package buttondevteam.lib.architecture;

import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration is based on class name
 */
public abstract class Component {
	private static HashMap<Class<? extends Component>, Component> components = new HashMap<>();

	@Getter
	private boolean enabled = false;
	@Getter(value = AccessLevel.PROTECTED)
	@NonNull
	private JavaPlugin plugin;
	@NonNull
	private ConfigurationSection config;

	public ConfigData<Boolean> shouldBeEnabled() {
		return getData("enabled", true);
	}

	private HashMap<String, ConfigData<?>> datamap = new HashMap<>();

	/**
	 * This method overload should only be used with primitves or String.
	 *
	 * @param path The path in config to use
	 * @param def  The value to use by default
	 * @param <T>  The type of this variable (only use primitives or String)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	protected <T> ConfigData<T> getData(String path, T def) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def));
		return (ConfigData<T>) data;
	}

	/**
	 * This method overload may be used with any class.
	 *
	 * @param path   The path in config to use
	 * @param def    The value to use by default
	 * @param getter A function that converts a primitive representation to the correct value
	 * @param setter A function that converts a value to a primitive representation
	 * @param <T>    The type of this variable (can be any class)
	 * @return The data object that can be used to get or set the value
	 */
	@SuppressWarnings("unchecked")
	protected <T> ConfigData<T> getData(String path, T def, Function<Object, T> getter, Function<T, Object> setter) {
		ConfigData<?> data = datamap.get(path);
		if (data == null) datamap.put(path, data = new ConfigData<>(config, path, def, getter, setter));
		return (ConfigData<T>) data;
	}

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.<br>
	 * The component will be enabled automatically, regardless of when it was registered.
	 *
	 * @param component The component to register
	 */
	public static void registerComponent(JavaPlugin plugin, Component component) {
		registerUnregisterComponent(plugin, component, true);
	}

	/**
	 * Unregisters a component by calling {@link #unregister(JavaPlugin)}.<br>
	 * Make sure to unregister the dependencies last.
	 *
	 * @param componentClass The component class to unregister
	 */
	public static void unregisterComponent(JavaPlugin plugin, Class<? extends Component> componentClass) {
		val component = components.get(componentClass);
		if (component == null)
			return; //Failed to load
		registerUnregisterComponent(plugin, component, false);
	}

	public static void registerUnregisterComponent(JavaPlugin plugin, Component component, boolean register) {
		val metaAnn = component.getClass().getAnnotation(ComponentMetadata.class);
		if (metaAnn != null) {
			Class<? extends Component>[] dependencies = metaAnn.depends();
			for (val dep : dependencies) {
				if (!components.containsKey(dep)) {
					plugin.getLogger().warning("Failed to " + (register ? "" : "un") + "register component " + component.getClassName() + " as a required dependency is missing/disabled: " + dep.getSimpleName());
					return;
				}
			}
		}
		if (register) {
			component.plugin = plugin;
			var compconf = plugin.getConfig().getConfigurationSection("components");
			if (compconf == null) compconf = plugin.getConfig().createSection("components");
			component.config = compconf.getConfigurationSection(component.getClassName());
			if (component.config == null) component.config = compconf.createSection(component.getClassName());
			component.register(plugin);
			components.put(component.getClass(), component);
			if (ComponentManager.areComponentsEnabled() && component.shouldBeEnabled().get()) {
				try { //Enable components registered after the previous ones getting enabled
					setComponentEnabled(component, true);
				} catch (UnregisteredComponentException ignored) {
				}
			}
		} else {
			if (component.enabled) {
				component.disable();
				component.enabled = false;
			}
			component.unregister(plugin);
			components.remove(component.getClass());
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
		if (component.enabled = enabled)
			component.enable();
		else
			component.disable();
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
	protected void register(JavaPlugin plugin) {
	}

	/**
	 * Unregisters the module, when called by the JavaPlugin class.
	 * This gets fired when the plugin is disabled.
	 * Do any cleanups needed within this method.
	 *
	 * @param plugin Plugin object
	 */
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
	 * Registers a TBMCCommand to the plugin. Make sure to add it to plugin.yml and use {@link buttondevteam.lib.chat.CommandClass}.
	 *
	 * @param plugin      Main plugin responsible for stuff
	 * @param commandBase Custom coded command class
	 */
	protected void registerCommand(JavaPlugin plugin, TBMCCommandBase commandBase) {
		TBMCChatAPI.AddCommand(plugin, commandBase);
	}

	/**
	 * Registers a Listener to this plugin
	 *
	 * @param plugin   Main plugin responsible for stuff
	 * @param listener The event listener to register
	 * @return The provided listener
	 */
	protected Listener registerListener(JavaPlugin plugin, Listener listener) {
		TBMCCoreAPI.RegisterEventsForExceptions(listener, plugin);
		return listener;
	}

	public void saveData(FileConfiguration config, String pathToData, Object data) {
		config.set("moduledata." + this.getClassName() + "." + pathToData, data);
	}

	public Object getData(FileConfiguration config, String pathToData, Object data) {
		return config.get("moduledata." + this.getClassName() + "." + pathToData, data);
	}

	private String getClassName() {
		Class<?> enclosingClass = getClass().getEnclosingClass();
		String className;
		if (enclosingClass != null) {
			className = (enclosingClass.getName());
		} else {
			className = (getClass().getName());
		}
		return className;
	}
}
