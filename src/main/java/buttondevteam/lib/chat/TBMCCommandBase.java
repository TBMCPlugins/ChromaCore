package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class TBMCCommandBase {

	public TBMCCommandBase() {
	}

	public abstract String[] GetHelpText(String alias);

	public abstract boolean OnCommand(CommandSender sender, String alias, String[] args);

	public abstract String GetCommandPath();

	public abstract boolean GetPlayerOnly();

	public abstract boolean GetModOnly();

	Plugin plugin; // Used By TBMCChatAPI

	public Plugin getPlugin() { // Used by CommandCaller (ButtonChat)
		return plugin;
	}
}
