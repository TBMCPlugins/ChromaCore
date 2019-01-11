package buttondevteam.component.restart;

import buttondevteam.core.PrimeRestartCommand;
import buttondevteam.core.ScheduledRestartCommand;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;

public class RestartComponent extends Component {
	@Override
	public void enable() {
		//TODO: Permissions for the commands
		TBMCChatAPI.AddCommand(this, new ScheduledRestartCommand());
		TBMCChatAPI.AddCommand(this, new PrimeRestartCommand());
	}

	@Override
	public void disable() {

	}
}
