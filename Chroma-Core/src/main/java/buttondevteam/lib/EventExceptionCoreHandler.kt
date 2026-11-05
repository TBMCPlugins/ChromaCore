package buttondevteam.lib;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

class EventExceptionCoreHandler extends EventExceptionHandler {

	@Override
	public boolean handle(Throwable ex, Event event) {
		TBMCCoreAPI.SendException("An error occured while executing " + event.getEventName() + "!", ex, false, Bukkit.getLogger()::warning);
		return true;
	}

}
