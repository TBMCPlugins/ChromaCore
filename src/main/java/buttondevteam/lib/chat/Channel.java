package buttondevteam.lib.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

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
	 *            Checks all senders against the criteria provided here and sends the message if the index matches the sender's - if no score at all, displays the error.<br>
	 *            May be null to send to everyone.
	 */
	public Channel(String displayname, Color color, String command,
			Function<CommandSender, RecipientTestResult> filteranderrormsg) {
		DisplayName = displayname;
		this.color = color;
		ID = command;
		this.filteranderrormsg = filteranderrormsg;
	}

	public static List<Channel> getChannels() {
		return channels;
	}

	public static Channel GlobalChat;
	public static Channel AdminChat;
	public static Channel ModChat;

	static void RegisterChannel(Channel channel) {
		channels.add(channel);
		Bukkit.getPluginManager().callEvent(new ChatChannelRegisterEvent(channel));
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
		}
	}
}
