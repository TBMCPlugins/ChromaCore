package buttondevteam.core.component.updater;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;

/**
 * Downloads plugin updates built from their source using JitPack - doesn't work anymore
 */
@ComponentMetadata(enabledByDefault = false)
public class PluginUpdaterComponent extends Component<MainPlugin> { //TODO: Config
	@Override
	public void enable() {
		registerCommand(new UpdatePluginCommand());
	}

	@Override
	public void disable() { //Commands are automatically unregistered

	}
}
