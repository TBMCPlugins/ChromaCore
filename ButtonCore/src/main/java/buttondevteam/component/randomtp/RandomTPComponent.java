package buttondevteam.component.randomtp;

import buttondevteam.lib.architecture.Component;

public class RandomTPComponent extends Component {
	@Override
	protected void enable() {
		new RandomTP().onEnable(this); //It registers it's command
	}

	@Override
	protected void disable() {

	}
}
