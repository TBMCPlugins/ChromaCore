package buttondevteam.lib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>
 * This event gets called (ideally) each time an exception occurs in a TBMC plugin. To call it, use {@link TBMCCoreAPI#SendException(String, Exception)}.
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCExceptionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private String sourcemsg;
	private Throwable exception;
	private boolean handled;

	TBMCExceptionEvent(String sourcemsg, Throwable exception) {
		this.sourcemsg = sourcemsg;
		this.exception = exception;
	}

	/**
	 * Gets the source message (where did this exception occur, etc.)
	 * 
	 * @return The message
	 */
	public String getSourceMessage() {
		return sourcemsg;
	}

	/**
	 * Gets the exception
	 * 
	 * @return The exception
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * Gets if this event was handled
	 * 
	 * @return True if it was handled
	 */
	public boolean isHandled() {
		return handled;
	}

	/**
	 * Flags the event as handled
	 */
	public void setHandled() {
		this.handled = true;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
