package buttondevteam.lib;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Channel.RecipientTestResult;

/**
 * Make sure to only send the message to users who {@link #shouldSendTo(CommandSender)} returns true.
 * 
 * @author NorbiPeti
 *
 */
public class TBMCChatEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Channel channel;
	private CommandSender sender;
	private String message;
	private boolean cancelled;
	private int score;

	public TBMCChatEvent(CommandSender sender, Channel channel, String message, int score) {
		this.sender = sender;
		this.channel = channel;
		this.message = message; // TODO: Message object with data?
		this.score = score;
	}

	/*
	 * public TBMCPlayer getPlayer() { return TBMCPlayer.getPlayer(sender); // TODO: Get Chroma user }
	 */

	public Channel getChannel() {
		return channel;
	}

	public CommandSender getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Note: Errors are sent to the sender automatically
	 */
	public boolean shouldSendTo(CommandSender sender) {
		if (channel.filteranderrormsg == null)
			return true;
		RecipientTestResult result = channel.filteranderrormsg.apply(sender);
		return result.errormessage == null && score == result.score;
	}

	/**
	 * Note: Errors are sent to the sender automatically
	 */
	public int getMCScore(CommandSender sender) {
		if (channel.filteranderrormsg == null)
			return 0;
		RecipientTestResult result = channel.filteranderrormsg.apply(sender);
		return result.errormessage == null ? result.score : -1;
	}
}
