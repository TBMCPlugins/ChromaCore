package buttondevteam.lib.architecture;

import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException;
import buttondevteam.lib.chat.ICommand2MC;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Configuration is based on class name
 */
@HasConfig //Used for obtaining javadoc
public abstract class Component<TP extends JavaPlugin> {
	private static HashMap<Class<? extends Component>, Component<? extends JavaPlugin>> components = new HashMap<>();

	@Getter
	private boolean enabled = false;
	@Getter
	@NonNull
	private TP plugin;
	@NonNull
	private @Getter
	IHaveConfig config;
	private @Getter IHaveConfig data; //TODO

	public final ConfigData<Boolean> shouldBeEnabled() {
		return config.getData("enabled", true);
	}

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.<br>
	 * The component will be enabled automatically, regardless of when it was registered.<br>
	 *     <b>If not using {@link ButtonPlugin}, call {@link ComponentManager#unregComponents(ButtonPlugin)} on plugin disable.</b>
	 *
	 * @param component The component to register
	 * @return Whether the component is registered successfully (it may have failed to enable)
	 */
	public static <T extends JavaPlugin> boolean registerComponent(T plugin, Component<T> component) {
		return registerUnregisterComponent(plugin, component, true);
	}

	/**
	 * Unregisters a component by calling {@link #unregister(JavaPlugin)}.<br>
	 * Make sure to unregister the dependencies last.<br>
	 *     <b>Components will be unregistered in opposite order of registering by default by {@link ButtonPlugin} or {@link ComponentManager#unregComponents(ButtonPlugin)}.</b>
	 *
	 * @param component The component to unregister
	 * @return Whether the component is unregistered successfully (it also got disabled)
	 */
	public static <T extends JavaPlugin> boolean unregisterComponent(T plugin, Component<T> component) {
		return registerUnregisterComponent(plugin, component, false);
	}

	public static <T extends JavaPlugin> boolean registerUnregisterComponent(T plugin, Component<T> component, boolean register) {
		try {
			val metaAnn = component.getClass().getAnnotation(ComponentMetadata.class);
			if (metaAnn != null) {
				Class<? extends Component>[] dependencies = metaAnn.depends();
				for (val dep : dependencies) { //TODO: Support dependencies at enable/disable as well
					if (!components.containsKey(dep)) {
						plugin.getLogger().warning("Failed to " + (register ? "" : "un") + "register component " + component.getClassName() + " as a required dependency is missing/disabled: " + dep.getSimpleName());
						return false;
					}
				}
			}
			if (register) {
				if (components.containsKey(component.getClass())) {
					TBMCCoreAPI.SendException("Failed to register component " + component.getClassName(), new IllegalArgumentException("The component is already registered!"));
					return false;
				}
				component.plugin = plugin;
				updateConfig(plugin, component);
				component.register(plugin);
				components.put(component.getClass(), component);
				if (plugin instanceof ButtonPlugin)
					((ButtonPlugin) plugin).getComponentStack().push(component);
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
				if (!components.containsKey(component.getClass()))
					return true; //Already unregistered
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
		if (component.enabled = enabled) {
			updateConfig(component.getPlugin(), component);
			component.enable();
		} else {
			component.disable();
			component.plugin.saveConfig();
			TBMCChatAPI.RemoveCommands(component);
		}
	}

	private static void updateConfig(JavaPlugin plugin, Component component) {
		if (plugin.getConfig() != null) { //Production
			var compconf = plugin.getConfig().getConfigurationSection("components");
			if (compconf == null) compconf = plugin.getConfig().createSection("components");
			var configSect = compconf.getConfigurationSection(component.getClassName());
			if (configSect == null)
				configSect = compconf.createSection(component.getClassName());
			component.config = new IHaveConfig(configSect);
		} else //Testing
			component.config = new IHaveConfig(null);
	}

	/**
	 * Returns the currently registered components<br>
	 *
	 * @return The currently registered components
	 */
	public static Map<Class<? extends Component>, Component<? extends JavaPlugin>> getComponents() {
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
	 * Registers a TBMCCommand to the component. Make sure to use {@link buttondevteam.lib.chat.CommandClass} and {@link buttondevteam.lib.chat.Command2.Subcommand}.
	 * You don't need to register the command in plugin.yml.
	 *
	 * @param commandBase Custom coded command class
	 */
	protected final void registerCommand(ICommand2MC commandBase) {
		ButtonPlugin.getCommand2MC().registerCommand(commandBase);
	}

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

	/**
	 * Returns a map of configs that are under the given key.
	 * @param key The key to use
	 * @param defaultProvider A mapping between config paths and config generators
	 * @return A map containing configs
	 */
	protected Map<String, IHaveConfig> getConfigMap(String key, Map<String, Consumer<IHaveConfig>> defaultProvider) {
		val c=getConfig().getConfig();
		var cs=c.getConfigurationSection(key);
		if(cs==null) cs=c.createSection(key);
		val res = cs.getValues(false).entrySet().stream().filter(e -> e.getValue() instanceof ConfigurationSection)
			.collect(Collectors.toMap(Map.Entry::getKey, kv -> new IHaveConfig((ConfigurationSection) kv.getValue())));
		if (res.size() == 0) {
			for (val entry : defaultProvider.entrySet()) {
				val conf = new IHaveConfig(cs.createSection(entry.getKey()));
				entry.getValue().accept(conf);
				res.put(entry.getKey(), conf);
			}
		}
		return res;
	}

	private String getClassName() {
		return getClass().getSimpleName();
	}
}
