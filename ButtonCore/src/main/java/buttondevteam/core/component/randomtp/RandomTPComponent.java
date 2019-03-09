package buttondevteam.core.component.randomtp;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;

/**
 * Teleport player to random location within world border.
 * Every five players teleport to the same general area, and then a new general area is randomly selected for the next five players.
 */
public class RandomTPComponent extends Component<MainPlugin> {
	@Override
	protected void enable() {
		new RandomTP().onEnable(this); //It registers it's command
	}

	@Override
	protected void disable() {

	}
}
