package buttondevteam.core.component.members;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static buttondevteam.core.MainPlugin.permission;

public class MemberComponent extends Component<MainPlugin> implements Listener {
	ConfigData<String> memberGroup() {
		return getConfig().getData("memberGroup", "member");
	}

	@Override
	protected void enable() {
		registerListener(this);
		registerCommand(new MemberCommand(this));
	}

	@Override
	protected void disable() {
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (permission != null && !permission.playerInGroup(event.getPlayer(), memberGroup().get())
			&& (new Date(event.getPlayer().getFirstPlayed()).toInstant().plus(7, ChronoUnit.DAYS).isBefore(Instant.now())
			|| event.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) > 20 * 3600 * 12)) {
			permission.playerAddGroup(null, event.getPlayer(), memberGroup().get());
			event.getPlayer().sendMessage("§bYou are a member now. YEEHAW");
			MainPlugin.Instance.getLogger().info("Added " + event.getPlayer().getName() + " as a member.");
		}
	}
}
