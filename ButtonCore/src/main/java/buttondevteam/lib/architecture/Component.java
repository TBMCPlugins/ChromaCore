package buttondevteam.lib.architecture;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.val;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public abstract class Component {
	private static HashMap<Class<? extends Component>, Component> components;

	/**
	 * Registers a component checking it's dependencies and calling {@link #register(JavaPlugin)}.<br>
	 * Make sure to register the dependencies first.
	 *
	 * @param component The component to register
	 */
	public static void registerComponent(JavaPlugin plugin, Component component) {
		val metaAnn = component.getClass().getAnnotation(ComponentMetadata.class);
		if (metaAnn != null) {
			Class<? extends Component>[] dependencies = metaAnn.depends();
			for (val dep : dependencies) {
				if (!components.containsKey(dep)) {
					plugin.getLogger().warning("Failed to register component " + component.getClassName() + " as a required dependency is missing/disabled: " + dep.getSimpleName());
					return;
				}
			}
		}
		component.register(plugin);
		components.put(component.getClass(), component);
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
		val metaAnn = componentClass.getAnnotation(ComponentMetadata.class);
		if (metaAnn != null) {
			Class<? extends Component>[] dependencies = metaAnn.depends();
			for (val dep : dependencies) {
				if (!components.containsKey(dep)) {
					plugin.getLogger().warning("Failed to unregister component " + component.getClassName() + " as a required dependency is missing/disabled: " + dep.getSimpleName());
					return;
				}
			}
		}
		component.unregister(plugin);
		components.remove(componentClass);
	}

	/**
	 * This is used to send a warning if there are registered components on shutdown.<br>
	 *
	 * @return If there are any registered components
	 */
	public static boolean haveRegisteredComponents() {
		return components.size() > 0;
	}

	/**
	 * Registers the module, when called by the JavaPlugin class. Call
	 * registerCommand() and registerListener() within this method.
	 *
	 * @param plugin Plugin class called to register commands and listeners
	 */
	public abstract void register(JavaPlugin plugin);

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
