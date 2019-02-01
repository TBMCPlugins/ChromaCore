package buttondevteam.core.component.updater;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

public class PluginUpdaterComponent extends Component {
	@Override
	public void enable() {
		TBMCChatAPI.AddCommand(this, new UpdatePluginCommand());
	}

	@Override
	public void disable() { //Commands are automatically unregistered

	}
}
