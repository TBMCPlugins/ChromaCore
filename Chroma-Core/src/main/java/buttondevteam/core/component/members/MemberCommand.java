package buttondevteam.core.component.members;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static buttondevteam.core.MainPlugin.permission;

@CommandClass(modOnly = true, path = "member", helpText = { //
	"Member command", //
	"Add or remove server members.", //
})
public class MemberCommand extends ICommand2MC {
	private final MemberComponent component;

	public MemberCommand(MemberComponent component) {
		getManager().addParamConverter(OfflinePlayer.class, Bukkit::getOfflinePlayer, "Player not found!");
		this.component = component;
	}

	@Command2.Subcommand
	public boolean add(CommandSender sender, OfflinePlayer player) {
		return addRemove(sender, player, true);
	}

	@Command2.Subcommand
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
	public void def(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("§cYou need to be a player to use this command.");
			return;
		}
		Player player= (Player) sender;
		String msg;
		if (component.checkMember(player))
			msg="You are a member.";
		else {
			component.getRegTime(player);
		}
	}
}
