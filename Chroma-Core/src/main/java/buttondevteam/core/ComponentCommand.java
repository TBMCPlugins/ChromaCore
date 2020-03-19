package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.Command2.Subcommand;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabCompleteMethod;
import buttondevteam.lib.chat.ICommand2MC;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@CommandClass(modOnly = true, helpText = {
	"Component command",
	"Can be used to enable/disable/list components"
})
public class ComponentCommand extends ICommand2MC {
	public ComponentCommand() {
		getManager().addParamConverter(Plugin.class, arg -> Bukkit.getPluginManager().getPlugin(arg), "Plugin not found!",
			() -> Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName)::iterator);
	}

	@Subcommand(helpText = {
		"Enable component",
		"Temporarily or permanently enables a component."
	})
	public boolean enable(CommandSender sender, Plugin plugin, String component, @Command2.OptionalArg boolean permanent) {
		if (plugin instanceof ButtonPlugin) {
			if (!((ButtonPlugin) plugin).justReload()) {
				sender.sendMessage("§cCouldn't reload config, check console.");
				return true;
			}
		} else
			plugin.reloadConfig(); //Reload config so the new config values are read - All changes are saved to disk on disable
		return enable_disable(sender, plugin, component, true, permanent);
	}

	@Subcommand(helpText = {
		"Disable component",
		"Temporarily or permanently disables a component."
	})
	public boolean disable(CommandSender sender, Plugin plugin, String component, @Command2.OptionalArg boolean permanent) {
		return enable_disable(sender, plugin, component, false, permanent);
	}

	@Subcommand(helpText = {
		"List components",
		"Lists all of the registered Chroma components"
	})
	public boolean list(CommandSender sender, @Command2.OptionalArg String plugin) {
		sender.sendMessage("§6List of components:");
		Component.getComponents().values().stream().filter(c -> plugin == null || c.getPlugin().getName().equalsIgnoreCase(plugin)) //If plugin is null, don't check
			.map(c -> c.getPlugin().getName() + " - " + c.getClass().getSimpleName() + " - " + (c.isEnabled() ? "en" : "dis") + "abled").forEach(sender::sendMessage);
		return true;
	}

	@CustomTabCompleteMethod(param = "component", subcommand = {"enable", "disable"})
	public Iterable<String> componentTabcomplete(Plugin plugin) {
		return getPluginComponents(plugin).map(c -> c.getClass().getSimpleName())::iterator;
	}

	@CustomTabCompleteMethod(param = "plugin")
	public Iterable<String> list() {
		return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName)::iterator;
	}

	private boolean enable_disable(CommandSender sender, Plugin plugin, String component, boolean enable, boolean permanent) {
		try {
			val oc = getComponentOrError(plugin, component, sender);
			if (!oc.isPresent())
				return true;
			Component.setComponentEnabled(oc.get(), enable);
			if (permanent)
				oc.get().shouldBeEnabled().set(enable);
			sender.sendMessage(oc.get().getClass().getSimpleName() + " " + (enable ? "en" : "dis") + "abled " + (permanent ? "permanently" : "temporarily") + ".");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Couldn't " + (enable ? "en" : "dis") + "able component " + component + "!", e);
		}
		return true;
	}

	private Stream<Component<? extends JavaPlugin>> getPluginComponents(Plugin plugin) {
		return Component.getComponents().values().stream()
			.filter(c -> plugin.getName().equals(c.getPlugin().getName()));
	}

	private Optional<Component<?>> getComponentOrError(Plugin plugin, String arg, CommandSender sender) {
		val oc = getPluginComponents(plugin).filter(c -> c.getClass().getSimpleName().equalsIgnoreCase(arg)).findAny();
		if (!oc.isPresent())
			sender.sendMessage("§cComponent not found!"); //^ Much simpler to solve in the new command system
		return oc;
	}
}
