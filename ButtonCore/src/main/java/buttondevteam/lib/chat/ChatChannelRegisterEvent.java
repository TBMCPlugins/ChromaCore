package buttondevteam.lib.chat;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatChannelRegisterEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

    private final Channel channel;

	public ChatChannelRegisterEvent(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
