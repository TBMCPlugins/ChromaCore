package buttondevteam.core.component.randomtp;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;

public class RandomTPComponent extends Component<MainPlugin> {
	@Override
	protected void enable() {
		new RandomTP().onEnable(this); //It registers it's command
	}

	@Override
	protected void disable() {

	}
}
