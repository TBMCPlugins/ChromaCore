package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.Command2.Subcommand;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@CommandClass(modOnly = true, helpText = {
	"§6---- Component command ----",
	"Can be used to enable/disable/list components"
})
public class ComponentCommand extends ICommand2MC {
	public ComponentCommand() {
		getManager().addParamConverter(Plugin.class, arg -> Bukkit.getPluginManager().getPlugin(arg));
	}

	@Subcommand
	public boolean enable(CommandSender sender, Plugin plugin, String component) {
		if (plugin == null) return respond(sender, "§cPlugin not found!");
		plugin.reloadConfig(); //Reload config so the new config values are read - All changes are saved to disk on disable
		return enable_disable(sender, plugin, component, true);
	}

	@Subcommand
	public boolean disable(CommandSender sender, Plugin plugin, String component) {
		if (plugin == null) return respond(sender, "§cPlugin not found!");
		return enable_disable(sender, plugin, component, false);
	}

	@Subcommand
	public boolean list(CommandSender sender, String plugin) {
		sender.sendMessage("§6List of components:");
		Component.getComponents().values().stream().filter(c -> plugin == null || c.getPlugin().getName().equalsIgnoreCase(plugin)) //If plugin is null, don't check
			.map(c -> c.getPlugin().getName() + " - " + c.getClass().getSimpleName() + " - " + (c.isEnabled() ? "en" : "dis") + "abled").forEach(sender::sendMessage);
		return true;
	}

	private boolean enable_disable(CommandSender sender, Plugin plugin, String component, boolean enable) {
		try {
			val oc = getComponentOrError(plugin, component, sender);
			if (!oc.isPresent())
				return true;
			Component.setComponentEnabled(oc.get(), enable);
			sender.sendMessage(oc.get().getClass().getSimpleName() + " " + (enable ? "en" : "dis") + "abled.");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Couldn't " + (enable ? "en" : "dis") + "able component " + component + "!", e);
		}
		return true;
	}

	private Optional<Component> getComponentOrError(Plugin plugin, String arg, CommandSender sender) {
		val oc = Component.getComponents().values().stream()
			.filter(c -> plugin.getName().equals(c.getPlugin().getName()))
			.filter(c -> c.getClass().getSimpleName().equalsIgnoreCase(arg)).findAny();
		if (!oc.isPresent())
			sender.sendMessage("§cComponent not found!"); //^ Much simpler to solve in the new command system
		return oc;
	} //TODO: Tabcompletion for the new command system
}
