package buttondevteam.core;

import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = true)
public class ComponentCommand extends TBMCCommandBase {
	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length < 2)
			return false;
		switch (args[0]) {
			case "enable":
				break;
			case "disable":
				break;
			case "list":
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[0];
	}
}
