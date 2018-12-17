package buttondevteam.lib.architecture;

import buttondevteam.lib.TBMCCoreAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ButtonPlugin extends JavaPlugin {
	private HashMap<String, ConfigData<?>> datamap = new HashMap<>();
	private ConfigurationSection section;

	protected abstract void pluginEnable();

	protected abstract void pluginDisable();

	@Override
	public void onEnable() {
		section = getConfig().getConfigurationSection("global");
		if (section == null) getConfig().createSection("global");
		try {
			pluginEnable();
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while enabling plugin " + getName() + "!", e);
		}
	}

	@Override
	public void onDisable() {
		try {
			pluginDisable();
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while disabling plugin " + getName() + "!", e);
		}
	}

	/**
	 * @see IHaveConfig#getData(Map, ConfigurationSection, String, Object)
	 */
	protected <T> ConfigData<T> getData(String path, T def) {
		return IHaveConfig.getData(datamap, section, path, def);
	}

	/**
	 * @see IHaveConfig#getData(Map, ConfigurationSection, String, Object, Function, Function)
	 */
	protected <T> ConfigData<T> getData(String path, T def, Function<Object, T> getter, Function<T, Object> setter) {
		return IHaveConfig.getData(datamap, section, path, def, getter, setter);
	}
}
