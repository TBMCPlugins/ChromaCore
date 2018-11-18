package buttondevteam.component.updater;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

public class PluginUpdaterComponent extends Component {
	@Override
	public void enable() {
		TBMCChatAPI.AddCommand(getPlugin(), UpdatePluginCommand.class);
	}

	@Override
	public void disable() { //TODO: Unregister commands and such

	}
}
