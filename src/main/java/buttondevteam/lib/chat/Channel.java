package buttondevteam.lib.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import buttondevteam.core.MainPlugin;

public class Channel {
	public final String DisplayName;
	public final Color color;
	public final String ID;
	/**
	 * Filters both the sender and the targets
	 */
	public final Function<CommandSender, RecipientTestResult> filteranderrormsg;

	private static List<Channel> channels = new ArrayList<>();

	/**
	 * Creates a channel.
	 * 
	 * @param displayname
	 *            The name that should appear at the start of the message
	 * @param color
	 *            The default color of the messages sent in the channel
	 * @param command
	 *            The command to be used for the channel <i>without /</i>. For example "mod". It's also used for scoreboard objective names.
	 * @param filteranderrormsg
	 *            Checks all senders against the criteria provided here and sends the message returned if it has an index - otherwise displays the error.<br>
	 *            May be null to send to everyone.
	 */
	public Channel(String displayname, Color color, String command,
			Function<CommandSender, RecipientTestResult> filteranderrormsg) {
		DisplayName = displayname;
		this.color = color;
		ID = command;
		this.filteranderrormsg = filteranderrormsg;
	}

	static {
		RegisterChannel(GlobalChat = new Channel("§fg§f", Color.White, "g", null));
		RegisterChannel(TownChat = new Channel("§3TC§f", Color.DarkAqua, "tc", s -> checkTownNationChat(s, false)));
		RegisterChannel(NationChat = new Channel("§6NC§f", Color.Gold, "nc", s -> checkTownNationChat(s, true)));
		RegisterChannel(AdminChat = new Channel("§cADMIN§f", Color.Red, "a", s -> s.isOp() ? new RecipientTestResult(0)
				: new RecipientTestResult("You need to be an admin to use this channel.")));
		RegisterChannel(ModChat = new Channel("§9MOD§f", Color.Blue, "mod",
				s -> s.isOp() || (s instanceof Player && MainPlugin.permission.playerInGroup((Player) s, "mod"))
						? new RecipientTestResult(0) //
						: new RecipientTestResult("You need to be a mod to use this channel.")));
	}

	public static List<Channel> getChannels() {
		return channels;
	}

	public static Channel GlobalChat;
	public static Channel TownChat;
	public static Channel NationChat;
	public static Channel AdminChat;
	public static Channel ModChat;

	static void RegisterChannel(Channel channel) {
		channels.add(channel);
		Bukkit.getPluginManager().callEvent(new ChatChannelRegisterEvent(channel));
	}

	/**
	 * Return the error message for the message sender if they can't send it and the score
	 */
	private static RecipientTestResult checkTownNationChat(CommandSender sender, boolean nationchat) {
		if (!(sender instanceof Player))
			return new RecipientTestResult("§cYou are not a player!");
		try {
			Resident resident = MainPlugin.TU.getResidentMap().get(sender.getName().toLowerCase());
			if (resident != null && resident.getModes().contains("spy"))
				return null;
			/*
			 * p.sendMessage(String.format("[SPY-%s] - %s: %s", channel.DisplayName, ((Player) sender).getDisplayName(), message));
			 */
			Town town = null;
			if (resident != null && resident.hasTown())
				town = resident.getTown();
			if (town == null)
				return new RecipientTestResult("You aren't in a town.");
			Nation nation = null;
			int index = -1; // TODO: Move all this to the event in ButtonChat along with the channel definitions and make scores only for the sender...
			if (nationchat) {
				if (town.hasNation())
					nation = town.getNation();
				if (nation == null)
					return new RecipientTestResult("Your town isn't in a nation.");
				index = MainPlugin.Nations.indexOf(nation);
				if (index < 0) {
					MainPlugin.Nations.add(nation);
					index = MainPlugin.Nations.size() - 1;
				}
			} else {
				index = MainPlugin.Towns.indexOf(town);
				if (index < 0) {
					MainPlugin.Towns.add(town);
					index = MainPlugin.Towns.size() - 1;
				}
			}
			return new RecipientTestResult(index);
		} catch (NotRegisteredException e) {
			return new RecipientTestResult("You (probably) aren't knwon by Towny! (Not in a town)");
		}
	}

	public static class RecipientTestResult {
		public String errormessage;
		public int score;

		/**
		 * Creates a result that indicates an <b>error</b>
		 * 
		 * @param errormessage
		 *            The error message to show the sender if they don't meet the criteria.
		 */
		public RecipientTestResult(String errormessage) {
			this.errormessage = errormessage;
		}

		/**
		 * Creates a result that indicates a <b>success</b>
		 * 
		 * @param score
		 *            The score that identifies the target group. For example, the index of the town or nation to send to.
		 */
		public RecipientTestResult(int score) {
			this.score = score;
		} // TODO: Set score of players, if they get one, otherwise (on error) set score to -1
	}
}
