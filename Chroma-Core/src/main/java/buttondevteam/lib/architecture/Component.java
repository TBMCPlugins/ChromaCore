package buttondevteam.lib.architecture;

import buttondevteam.buttonproc.HasConfig;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.exceptions.UnregisteredComponentException;
import buttondevteam.lib.chat.ICommand2MC;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Configuration is based on class name
 */
@HasConfig(global = false) //Used for obtaining javadoc
public abstract class Component<TP extends JavaPlugin> {
	@SuppressWarnings("rawtypes") private static HashMap<Class<? extends Component>, Component<? extends JavaPlugin>> components = new HashMap<>();

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
		return config.getData("enabled", Optional.ofNullable(getClass().getAnnotation(ComponentMetadata.class)).map(ComponentMetadata::enabledByDefault).orElse(true));
	}

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.<br>
	 * The component will be enabled automatically, regardless of when it was registered.<br>
	 * <b>If not using {@link ButtonPlugin}, call {@link ComponentManager#unregComponents(ButtonPlugin)} on plugin disable.</b>
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
	 * <b>Components will be unregistered in opposite order of registering by default by {@link ButtonPlugin} or {@link ComponentManager#unregComponents(ButtonPlugin)}.</b>
	 *
	 * @param component The component to unregister
	 * @return Whether the component is unregistered successfully (it also got disabled)
	 */
	public static <T extends ButtonPlugin> boolean unregisterComponent(T plugin, Component<T> component) {
		return registerUnregisterComponent(plugin, component, false);
	}

	public static <T extends JavaPlugin> boolean registerUnregisterComponent(T plugin, Component<T> component, boolean register) {
		try {
			val metaAnn = component.getClass().getAnnotation(ComponentMetadata.class);
			if (metaAnn != null) {
				@SuppressWarnings("rawtypes") Class<? extends Component>[] dependencies = metaAnn.depends();
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
			}
			return true;
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to " + (register ? "" : "un") + "register component " + component.getClassName() + "!", e);
			return false;
		}
	}

	/**
	 * Enables or disables the given component. If the component fails to enable, it will be disabled.
	 *
	 * @param component The component to register
	 * @param enabled   Whether it's enabled or not
	 */
	public static void setComponentEnabled(Component<?> component, boolean enabled) throws UnregisteredComponentException {
		if (!components.containsKey(component.getClass()))
			throw new UnregisteredComponentException(component);
		if (component.enabled == enabled) return; //Don't do anything
		if (component.enabled = enabled) {
			try {
				updateConfig(component.getPlugin(), component);
				component.enable();
				if (ButtonPlugin.configGenAllowed(component)) {
					IHaveConfig.pregenConfig(component, null);
				}
			} catch (Exception e) {
				try { //Automatically disable components that fail to enable properly
					setComponentEnabled(component, false);
					throw e;
				} catch (Exception ex) {
					Throwable t = ex;
					for (var th = t; th != null; th = th.getCause())
						t = th; //Set if not null
					if (t != e)
						t.initCause(e);
					throw ex;
				}
			}
		} else {
			component.disable();
			ButtonPlugin.getCommand2MC().unregisterCommands(component);
		}
	}

	public static void updateConfig(JavaPlugin plugin, Component component) {
		if (plugin.getConfig() != null) { //Production
			var compconf = plugin.getConfig().getConfigurationSection("components");
			if (compconf == null) compconf = plugin.getConfig().createSection("components");
			var configSect = compconf.getConfigurationSection(component.getClassName());
			if (configSect == null)
				configSect = compconf.createSection(component.getClassName());
			if (component.config != null) component.config.reset(configSect);
			else component.config = new IHaveConfig(configSect, plugin::saveConfig);
		} else //Testing
			if (component.config == null)
				component.config = new IHaveConfig(null, plugin::saveConfig);
	}

	/**
	 * Returns the currently registered components<br>
	 *
	 * @return The currently registered components
	 */
	@SuppressWarnings("rawtypes")
	public static Map<Class<? extends Component>, Component<? extends JavaPlugin>> getComponents() {
		return Collections.unmodifiableMap(components);
	}

	public void log(String message) {
		plugin.getLogger().info("[" + getClassName() + "] " + message);
	}

	public void logWarn(String message) {
		plugin.getLogger().warning("[" + getClassName() + "] " + message);
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
	 * To access the plugin, use {@link #getPlugin()}.
	 */
	protected abstract void disable();

	/**
	 * Registers a command to the component. Make sure to use {@link buttondevteam.lib.chat.CommandClass} and {@link buttondevteam.lib.chat.Command2.Subcommand}.
	 * You don't need to register the command in plugin.yml.
	 *
	 * @param command Custom coded command class
	 */
	protected final void registerCommand(ICommand2MC command) {
		if (plugin instanceof ButtonPlugin)
			command.registerToPlugin((ButtonPlugin) plugin);
		command.registerToComponent(this);
		ButtonPlugin.getCommand2MC().registerCommand(command);
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
	 *
	 * @param key             The key to use
	 * @param defaultProvider A mapping between config paths and config generators
	 * @return A map containing configs
	 */
	protected Map<String, IHaveConfig> getConfigMap(String key, Map<String, Consumer<IHaveConfig>> defaultProvider) {
		val c = getConfig().getConfig();
		var cs = c.getConfigurationSection(key);
		if (cs == null) cs = c.createSection(key);
		val res = cs.getValues(false).entrySet().stream().filter(e -> e.getValue() instanceof ConfigurationSection)
			.collect(Collectors.toMap(Map.Entry::getKey, kv -> new IHaveConfig((ConfigurationSection) kv.getValue(), getPlugin()::saveConfig)));
		if (res.size() == 0) {
			for (val entry : defaultProvider.entrySet()) {
				val conf = new IHaveConfig(cs.createSection(entry.getKey()), getPlugin()::saveConfig);
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
