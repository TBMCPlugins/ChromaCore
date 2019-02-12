package buttondevteam.core.component.channel;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class ChannelComponent extends Component {
	static TBMCSystemChatEvent.BroadcastTarget roomJoinLeave;
	@Override
	protected void register(JavaPlugin plugin) {
		super.register(plugin);
		roomJoinLeave = TBMCSystemChatEvent.BroadcastTarget.add("roomJoinLeave"); //Even if it's disabled, global channels continue to work
	}

	@Override
	protected void unregister(JavaPlugin plugin) {
		super.unregister(plugin);
		TBMCSystemChatEvent.BroadcastTarget.remove(roomJoinLeave);
		roomJoinLeave = null;
	}

	@Override
	protected void enable() {
	}

	@Override
	protected void disable() {

	}
}
