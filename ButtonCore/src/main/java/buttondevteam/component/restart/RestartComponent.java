package buttondevteam.component.restart;

import buttondevteam.core.PrimeRestartCommand;
import buttondevteam.core.ScheduledRestartCommand;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class RestartComponent extends Component {
	@Override
	public void register(JavaPlugin plugin) {
		//TODO: Separately (dis)allow commands
		TBMCChatAPI.AddCommand(plugin, ScheduledRestartCommand.class);
		TBMCChatAPI.AddCommand(plugin, PrimeRestartCommand.class);
	}

	@Override
	public void unregister(JavaPlugin plugin) {

	}
}
