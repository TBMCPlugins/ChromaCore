package buttondevteam.core;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.util.Optional;

@CommandClass(modOnly = true)
public class ComponentCommand extends TBMCCommandBase {
	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length < 1)
			return false;
		boolean enable = true;
		try {
			switch (args[0]) {
				case "enable":
					enable = true;
					break;
				case "disable":
					enable = false;
					break;
				case "list":
					sender.sendMessage("§6List of components:");
					Component.getComponents().values().stream().map(c -> c.getPlugin().getName() + " - " + c.getClass().getSimpleName() + " - " + (c.isEnabled() ? "en" : "dis") + "abled").forEach(sender::sendMessage);
					return true;
				default:
					return false;
			}
			if (args.length < 2)
				return false;
			val oc = getComponentOrError(args[1], sender);
			if (!oc.isPresent())
				return true;
			if (enable) //Reload config so the new config values are read
				getPlugin().reloadConfig(); //All changes are saved to disk on disable
			Component.setComponentEnabled(oc.get(), enable);
			sender.sendMessage(oc.get().getClass().getSimpleName() + " " + (enable ? "en" : "dis") + "abled.");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Couldn't " + (enable ? "en" : "dis") + "able component " + args[0] + "!", e);
		}
		return true;
	}

	private Optional<Component> getComponentOrError(String arg, CommandSender sender) {
		val oc = Component.getComponents().values().stream().filter(c -> c.getClass().getSimpleName().equalsIgnoreCase(arg)).findAny();
		if (!oc.isPresent())
			sender.sendMessage("§cComponent not found!");
		return oc;
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{
			"§6---- Component command ----",
			"Enable or disable or list components"
		};
	}
}
