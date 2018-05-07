package buttondevteam.lib;

import buttondevteam.lib.chat.Channel;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Can be used to change messages before it's sent.
 * <b>Only called before sending messages with SendChatMessage.</b>
 * 
 * @author NorbiPeti
 *
 */
public class TBMCChatPreprocessEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private Channel channel;
	private CommandSender sender;
	private String message;
	private boolean cancelled;

	public TBMCChatPreprocessEvent(CommandSender sender, Channel channel, String message) {
		this.sender = sender;
		this.channel = channel;
		this.message = message; // TODO: Message object with data?
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

	public void setMessage(String message) {
		this.message = message;
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
}
