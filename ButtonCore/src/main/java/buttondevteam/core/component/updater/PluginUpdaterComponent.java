package buttondevteam.core.component.updater;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

public class PluginUpdaterComponent extends Component<MainPlugin> {
	@Override
	public void enable() {
		TBMCChatAPI.AddCommand(this, new UpdatePluginCommand());
	}

	@Override
	public void disable() { //Commands are automatically unregistered

	}
}
