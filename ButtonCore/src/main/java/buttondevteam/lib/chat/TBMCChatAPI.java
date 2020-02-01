package buttondevteam.lib.chat;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.channel.Channel.RecipientTestResult;
import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCChatPreprocessEvent;
import buttondevteam.lib.TBMCSystemChatEvent;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.function.Supplier;

public class TBMCChatAPI {
	/**
	 * Sends a chat message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.<br>
	 * This will also send the error message to the sender, if they can't send the message.
	 *
	 * @param cm The message to send
	 * @return The event cancelled state
	 */
	public static boolean SendChatMessage(ChatMessage cm) {
		return SendChatMessage(cm, cm.getUser().channel().get());
	}

	/**
	 * Sends a chat message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.<br>
	 * This will also send the error message to the sender, if they can't send the message.
	 *
	 * @param cm      The message to send
	 * @param channel The MC channel to send in
	 * @return The event cancelled state
	 */
	public static boolean SendChatMessage(ChatMessage cm, Channel channel) {
		if (!Channel.getChannelList().contains(channel))
			throw new RuntimeException("Channel " + channel.DisplayName().get() + " not registered!");
		if (!channel.Enabled().get()) {
			cm.getSender().sendMessage("§cThe channel '" + channel.DisplayName().get() + "' is disabled!");
			return true; //Cancel sending if channel is disabled
		}
		Supplier<Boolean> task = () -> {
			val permcheck = cm.getPermCheck();
			RecipientTestResult rtr = getScoreOrSendError(channel, permcheck);
			int score = rtr.score;
			if (score == Channel.SCORE_SEND_NOPE || rtr.groupID == null)
				return true;
			TBMCChatPreprocessEvent eventPre = new TBMCChatPreprocessEvent(cm.getSender(), channel, cm.getMessage());
			Bukkit.getPluginManager().callEvent(eventPre);
			if (eventPre.isCancelled())
				return true;
			cm.setMessage(eventPre.getMessage());
			TBMCChatEvent event;
			event = new TBMCChatEvent(channel, cm, rtr);
			Bukkit.getPluginManager().callEvent(event);
			return event.isCancelled();
		};
		return ChromaUtils.doItAsync(task, false); //Not cancelled if async
	}

	/**
	 * Sends a regular message to Minecraft. Make sure that the channel is registered with {@link #RegisterChatChannel(Channel)}.
	 *
	 * @param channel    The channel to send to
	 * @param rtr        The score&group to use to find the group - use {@link RecipientTestResult#ALL} if the channel doesn't have scores
	 * @param message    The message to send
	 * @param exceptions Platforms where this message shouldn't be sent (same as {@link ChatMessage#getOrigin()}
	 * @return The event cancelled state
	 */
	public static boolean SendSystemMessage(Channel channel, RecipientTestResult rtr, String message, TBMCSystemChatEvent.BroadcastTarget target, String... exceptions) {
		if (!Channel.getChannelList().contains(channel))
			throw new RuntimeException("Channel " + channel.DisplayName().get() + " not registered!");
		if (!channel.Enabled().get())
			return true; //Cancel sending
		if (!Arrays.asList(exceptions).contains("Minecraft"))
			Bukkit.getConsoleSender().sendMessage("[" + channel.DisplayName().get() + "] " + message);
		TBMCSystemChatEvent event = new TBMCSystemChatEvent(channel, message, rtr.score, rtr.groupID, exceptions, target);
		return ChromaUtils.callEventAsync(event);
	}

	private static RecipientTestResult getScoreOrSendError(Channel channel, CommandSender sender) {
		RecipientTestResult result = channel.getRTR(sender);
		if (result.errormessage != null)
			sender.sendMessage("§c" + result.errormessage);
		return result;
	}

	/**
	 * Register a chat channel. See {@link Channel#Channel(String, Color, String, java.util.function.Function)} for details.
	 *
	 * @param channel A new {@link Channel} to register
	 */
	public static void RegisterChatChannel(Channel channel) {
		Channel.RegisterChannel(channel);
	}
}
