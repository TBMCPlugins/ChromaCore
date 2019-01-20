package buttondevteam.component.members;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = true, path = "member")
public class MemberCommand extends TBMCCommandBase {
	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length < 2)
			return false;
		final boolean add;
		if (args[0].equalsIgnoreCase("add"))
			add = true;
		else if (args[0].equalsIgnoreCase("remove"))
			add = false;
		else
			return false;
		Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
			if (MainPlugin.permission == null) {
				sender.sendMessage("§cError: No permission plugin found!");
				return;
			}
			val op = Bukkit.getOfflinePlayer(args[1]);
			if (!op.hasPlayedBefore()) {
				sender.sendMessage("§cCannot find player or haven't played before.");
				return;
			}
			if (add) {
				if (MainPlugin.permission.playerAddGroup(null, op, "member"))
					sender.sendMessage("§b" + op.getName() + " added as a member!");
				else
					sender.sendMessage("§cFailed to add " + op.getName() + " as a member!");
			} else {
				if (MainPlugin.permission.playerRemoveGroup(null, op, "member"))
					sender.sendMessage("§b" + op.getName() + " removed as a member!");
				else
					sender.sendMessage("§bFailed to remove " + op.getName() + " as a member!");
			}
		});
		return true;
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{ //
				"06---- Member ----", //
				"Add or remove server members.", //
				"Usage: /member <add|remove> <player>" //
		};
	}
}
