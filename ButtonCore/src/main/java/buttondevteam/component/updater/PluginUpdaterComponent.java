package buttondevteam.component.updater;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginUpdaterComponent extends Component {
	@Override
	public void register(JavaPlugin plugin) {
		TBMCChatAPI.AddCommand(plugin, UpdatePluginCommand.class);
	}

	@Override
	public void unregister(JavaPlugin plugin) {

	}
}
