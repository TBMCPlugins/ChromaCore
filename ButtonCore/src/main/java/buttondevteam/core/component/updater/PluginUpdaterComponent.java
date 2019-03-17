package buttondevteam.core.component.updater;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

/**
 * Downloads plugin updates built from their source using JitPack - older code
 */
public class PluginUpdaterComponent extends Component<MainPlugin> { //TODO: Config
	@Override
	public void enable() {
		TBMCChatAPI.AddCommand(this, new UpdatePluginCommand());
	}

	@Override
	public void disable() { //Commands are automatically unregistered

	}
}
