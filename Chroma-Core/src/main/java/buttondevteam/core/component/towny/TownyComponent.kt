package buttondevteam.core.component.towny;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;

/**
 * Provides a command to remove invalid Towny residents.
 */
public class TownyComponent extends Component<MainPlugin> {
	@Override
	protected void enable() {
		registerCommand(new RemoveResidentsCommand());
	}

	@Override
	protected void disable() {
	}
}
