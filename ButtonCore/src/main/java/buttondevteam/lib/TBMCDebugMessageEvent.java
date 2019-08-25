package buttondevteam.lib;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TBMCDebugMessageEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
    private final String message;
	private boolean sent;

	public TBMCDebugMessageEvent(String message) {
		super(!Bukkit.isPrimaryThread());
		this.message = message;
	}

	/**
	 * Gets the message (where did this exception occur, etc.)
	 * 
	 * @return The message
	 */
	public String getDebugMessage() {
		return message;
	}

	/**
	 * Gets if this event was handled
	 * 
	 * @return True if it was handled
	 */
	public boolean isSent() {
		return sent;
	}

	/**
	 * Flags the event as handled
	 */
	public void setSent() {
		this.sent = true;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
