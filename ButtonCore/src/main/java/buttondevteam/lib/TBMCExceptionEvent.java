package buttondevteam.lib;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>
 * This event gets called (ideally) each time an exception occurs in a TBMC plugin. To call it, use {@link TBMCCoreAPI#SendException(String, Throwable)}.
 * </p>
 * 
 * @author Norbi
 *
 */
@Getter
public class TBMCExceptionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final String sourceMessage;
	private final Throwable exception;
	private boolean handled;

	@java.beans.ConstructorProperties({"sourceMessage", "exception"})
	public TBMCExceptionEvent(String sourceMessage, Throwable exception) {
		super(true);
		this.sourceMessage = sourceMessage;
		this.exception = exception;
	}

	public void setHandled() {
		handled = true;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
