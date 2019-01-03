package buttondevteam.component.restart;

import buttondevteam.core.PrimeRestartCommand;
import buttondevteam.core.ScheduledRestartCommand;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

public class RestartComponent extends Component {
	@Override
	public void enable() {
		//TODO: Permissions for the commands
		TBMCChatAPI.AddCommand(getPlugin(), ScheduledRestartCommand.class);
		TBMCChatAPI.AddCommand(getPlugin(), PrimeRestartCommand.class);
	}

	@Override
	public void disable() {

	}
}
