package buttondevteam.lib.architecture;

import buttondevteam.buttonproc.HasConfig;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Command2MC;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;

@HasConfig(global = true)
public abstract class ButtonPlugin extends JavaPlugin {
	@Getter
	private static Command2MC command2MC = new Command2MC();
	@Getter(AccessLevel.PROTECTED)
	private IHaveConfig iConfig;
	private CommentedConfiguration yaml;
	@Getter(AccessLevel.PROTECTED)
	private IHaveConfig data; //TODO
	/**
	 * Used to unregister components in the right order - and to reload configs
	 */
	@Getter
	private Stack<Component<?>> componentStack = new Stack<>();
	;

	protected abstract void pluginEnable();

	/**
	 * Called after the components are unregistered
	 */
	protected abstract void pluginDisable();

	/**
	 * Called before the components are unregistered
	 */
	protected void pluginPreDisable() {
	}

	@Override
	public final void onEnable() {
		loadConfig();
		try {
			pluginEnable();
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while enabling plugin " + getName() + "!", e);
		}
		if (configGenAllowed(this)) //If it's not disabled (by default it's not)
			IHaveConfig.pregenConfig(this, null);
	}

	private void loadConfig() {
		var section = getConfig().getConfigurationSection("global");
		if (section == null) section = getConfig().createSection("global");
		iConfig = new IHaveConfig(section, this::saveConfig);
	}

	@Override
	public final void onDisable() {
		try {
			pluginPreDisable();
			ComponentManager.unregComponents(this);
			pluginDisable();
			if (ConfigData.saveNow(getConfig()))
				getLogger().info("Saved configuration changes.");
			iConfig = null; //Clearing the hashmap is not enough, we need to update the section as well
			TBMCChatAPI.RemoveCommands(this);
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while disabling plugin " + getName() + "!", e);
		}
	}

	@Override
	public void reloadConfig() {
		tryReloadConfig();
	}

	public boolean tryReloadConfig() {
		if (!justReload()) return false;
		loadConfig();
		componentStack.forEach(c -> Component.updateConfig(this, c));
		return true;
	}

	public boolean justReload() {
		if (yaml != null && ConfigData.saveNow(getConfig())) {
			getLogger().warning("Saved pending configuration changes to the file, didn't reload. Apply your changes again.");
			return false;
		}
		var file = new File(getDataFolder(), "config.yml");
		var yaml = new CommentedConfiguration(file);
		if (file.exists() && !yaml.load()) {
			getLogger().warning("Failed to load config! Check for syntax errors.");
			return false;
		}
		this.yaml = yaml;
		var res = getTextResource("configHelp.yml");
		if (res == null)
			return true;
		var yc = YamlConfiguration.loadConfiguration(res);
		for (var kv : yc.getValues(true).entrySet())
			if (kv.getValue() instanceof String)
				yaml.addComment(kv.getKey(), Arrays.stream(((String) kv.getValue()).split("\n"))
					.map(str -> "# " + str.trim()).toArray(String[]::new));
		return true;
	}

	@Override
	public FileConfiguration getConfig() {
		if (yaml == null)
			justReload(); //TODO: If it fails to load, it'll probably throw an NPE
		return yaml;
	}

	@Override
	public void saveConfig() {
		if (yaml != null)
			yaml.save();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface ConfigOpts {
		boolean disableConfigGen() default false;
	}

	public static boolean configGenAllowed(Object obj) {
		return !Optional.ofNullable(obj.getClass().getAnnotation(ConfigOpts.class))
			.map(ConfigOpts::disableConfigGen).orElse(false);
	}
}
