package buttondevteam.lib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * This event gets called (ideally) each time an exception occurs in a TBMC plugin. To call it, use {@link TBMCCoreAPI#SendException(String, Exception)}.
 * </p>
 * 
 * @author Norbi
 *
 */
@Getter
@RequiredArgsConstructor
public class TBMCExceptionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final String sourceMessage;
	private final Throwable exception;
	private boolean handled;

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
