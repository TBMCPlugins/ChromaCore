package buttondevteam.lib.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	 *            The name that should appear at the start of the message. <b>A chat color is expected at the beginning (§9).</b>
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

	/**
	 * Must be only called from a subclass - otherwise it'll throw an exception.
	 * 
	 * @see Channel#Channel(String, Color, String, Function)
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Channel> Channel(String displayname, Color color, String command,
			BiFunction<T, CommandSender, RecipientTestResult> filteranderrormsg) {
		DisplayName = displayname;
		this.color = color;
		ID = command;
		this.filteranderrormsg = s -> filteranderrormsg.apply((T) this, s);
	}

	public static List<Channel> getChannels() {
		return channels;
	}

	/**
	 * Convenience method for the function parameter of {@link #Channel(String, Color, String, Function)}. It checks if the sender is OP or optionally has the specified group. The error message is
	 * generated automatically.
	 * 
	 * @param permgroup
	 *            The group that can access the channel or <b>null</b> to only allow OPs.
	 * @return
	 */
	public static Function<CommandSender, RecipientTestResult> inGroupFilter(String permgroup) {
		return noScoreResult(
				s -> s.isOp() || (permgroup != null
						? s instanceof Player && MainPlugin.permission.playerInGroup((Player) s, permgroup) : false),
				"You need to be a(n) " + (permgroup != null ? permgroup : "OP") + " to use this channel.");
	}

	public static Function<CommandSender, RecipientTestResult> noScoreResult(Predicate<CommandSender> filter,
			String errormsg) {
		return s -> filter.test(s) ? new RecipientTestResult(0) : new RecipientTestResult(errormsg);
	}

	public static <T extends Channel> BiFunction<T, CommandSender, RecipientTestResult> noScoreResult(
			BiPredicate<T, CommandSender> filter, String errormsg) {
		return (this_, s) -> filter.test(this_, s) ? new RecipientTestResult(0) : new RecipientTestResult(errormsg);
	}

	public static Channel GlobalChat;
	public static Channel AdminChat;
	public static Channel ModChat;

	static void RegisterChannel(Channel channel) {
		channels.add(channel);
		Bukkit.getScheduler().runTask(MainPlugin.Instance,
				() -> Bukkit.getPluginManager().callEvent(new ChatChannelRegisterEvent(channel))); // Wait for server start
	}

	public static class RecipientTestResult {
		public String errormessage = null;
		public int score = -1; // Anything below 0 is "never send"

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
