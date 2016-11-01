package buttondevteam.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import buttondevteam.lib.TBMCPlayer.InfoTarget;

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
	private Exception exception;

	TBMCExceptionEvent(String sourcemsg, Exception exception) {
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
	public Exception getException() {
		return exception;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
