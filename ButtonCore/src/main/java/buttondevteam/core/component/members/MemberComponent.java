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
import java.util.AbstractMap;
import java.util.Date;

import static buttondevteam.core.MainPlugin.permission;

/**
 * Allows giving a 'member' group over some time elapsed OR played.
 */
public class MemberComponent extends Component<MainPlugin> implements Listener {
	/**
	 * The permission group to give to the player
	 */
	ConfigData<String> memberGroup() {
		return getConfig().getData("memberGroup", "member");
	}

	/**
	 * The amount of hours needed to play before promotion
	 */
	private ConfigData<Integer> playedHours() {
		return getConfig().getData("playedHours", 12);
	}

	/**
	 * The amount of days passed since first login
	 */
	private ConfigData<Integer> registeredForDays() {
		return getConfig().getData("registeredForDays", 7);
	}

	private AbstractMap.SimpleEntry<Statistic, Integer> playtime;

	@Override
	protected void enable() {
		registerListener(this);
		registerCommand(new MemberCommand(this));
		try {
			playtime = new AbstractMap.SimpleEntry<>(Statistic.valueOf("PLAY_ONE_MINUTE"), 60); //1.14
		} catch (IllegalArgumentException e) {
			playtime = new AbstractMap.SimpleEntry<>(Statistic.valueOf("PLAY_ONE_TICK"), 20 * 3600); //1.12
		}
	}

	@Override
	protected void disable() {
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (permission != null && !permission.playerInGroup(event.getPlayer(), memberGroup().get())
			&& (new Date(event.getPlayer().getFirstPlayed()).toInstant().plus(registeredForDays().get(), ChronoUnit.DAYS).isBefore(Instant.now())
			|| event.getPlayer().getStatistic(playtime.getKey()) > playtime.getValue() * playedHours().get())) {
			if (permission.playerAddGroup(null, event.getPlayer(), memberGroup().get())) {
				event.getPlayer().sendMessage("§bYou are a member now. YEEHAW");
				MainPlugin.Instance.getLogger().info("Added " + event.getPlayer().getName() + " as a member.");
			} else {
				MainPlugin.Instance.getLogger().warning("Failed to assign the member role! Please make sure the member group exists or disable the component if it's unused.");
			}
		}
	}

}
