package buttondevteam.lib.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerCommandBase extends TBMCCommandBase {
	public abstract boolean OnCommand(Player player, String alias, String[] args);

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (sender instanceof Player)
			return OnCommand((Player) sender, alias, args);
		sender.sendMessage("§cYou need to be a player to use this command.");
		return true;
	}
}
