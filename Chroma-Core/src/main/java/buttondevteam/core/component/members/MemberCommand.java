package buttondevteam.core.component.members;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@CommandClass(path = "member", helpText = { //
	"Member command", //
	"Add or remove server members.", //
})
public class MemberCommand extends ICommand2MC {
	private final MemberComponent component;

	public MemberCommand(MemberComponent component) {
		this.component = component;
	}

	@Command2.Subcommand(permGroup = Command2.Subcommand.MOD_GROUP)
	public boolean add(CommandSender sender, OfflinePlayer player) {
		return addRemove(sender, player, true);
	}

	@Command2.Subcommand(permGroup = Command2.Subcommand.MOD_GROUP)
	public boolean remove(CommandSender sender, OfflinePlayer player) {
		return addRemove(sender, player, false);
	}

	public boolean addRemove(CommandSender sender, OfflinePlayer op, boolean add) {
		Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.Instance, () -> {
			if (!op.hasPlayedBefore()) {
				sender.sendMessage("§cCannot find player or haven't played before.");
				return;
			}
			if (add ? MainPlugin.permission.playerAddGroup(null, op, component.memberGroup().get())
				: MainPlugin.permission.playerRemoveGroup(null, op, component.memberGroup().get()))
				sender.sendMessage("§b" + op.getName() + " " + (add ? "added" : "removed") + " as a member!");
			else
				sender.sendMessage("§cFailed to " + (add ? "add" : "remove") + " " + op.getName() + " as a member!");
		});
		return true;
	}

	@Command2.Subcommand
	public void def(Player player) {
		String msg;
		if (!component.checkNotMember(player))
			msg = "You are a member.";
		else {
			double pt = component.getPlayTime(player);
			long rt = component.getRegTime(player);
			if (pt == -1 || rt == -1) {
				Boolean result = component.addPlayerAsMember(player);
				if (result == null)
					msg = "Can't assign member group because groups are not supported by the permissions plugin.";
				else if (result)
					msg = "You meet all the requirements.";
				else
					msg = "You should be a member but failed to add you to the group.";
			} else
				msg = String.format("You need to play for %.2f hours total or play for %d more days to become a member.",
					pt, TimeUnit.MILLISECONDS.toDays(rt));
		}
		player.sendMessage(msg);
	}
}
