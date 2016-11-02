package buttondevteam.lib;

import org.bukkit.event.Event;

public class EventExceptionCoreHandler extends EventExceptionHandler {

	@Override
	public boolean handle(Throwable ex, Event event) {
		TBMCCoreAPI.SendException("An error occured while executing " + event.getEventName() + "!", ex);
		return true;
	}

}
