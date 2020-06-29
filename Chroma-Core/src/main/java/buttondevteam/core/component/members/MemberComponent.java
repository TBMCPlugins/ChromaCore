package buttondevteam.core.component.members;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.architecture.ConfigData;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
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
@ComponentMetadata(enabledByDefault = false)
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
		if (checkNotMember(event.getPlayer()) && (checkRegTime(event.getPlayer()) || checkPlayTime(event.getPlayer()))) {
			addPlayerAsMember(event.getPlayer());
		}
	}

	public Boolean addPlayerAsMember(Player player) {
		try {
			if (permission.playerAddGroup(null, player, memberGroup().get())) {
				player.sendMessage("§bYou are a member now!");
				log("Added " + player.getName() + " as a member.");
				return true;
			} else {
				logWarn("Failed to assign the member role! Please make sure the member group exists or disable the component if it's unused.");
				return false;
			}
		} catch (UnsupportedOperationException e) {
			logWarn("Failed to assign the member role! Groups are not supported by the permissions implementation.");
			return null;
		}
	}

	public boolean checkNotMember(Player player) {
		return permission != null && !permission.playerInGroup(player, memberGroup().get());
	}

	public boolean checkRegTime(Player player) {
		return getRegTime(player) == -1;
	}

	public boolean checkPlayTime(Player player) {
		return getPlayTime(player) > playtime.getValue() * playedHours().get();
	}

	/**
	 * Returns milliseconds
	 */
	public long getRegTime(Player player) {
		Instant date = new Date(player.getFirstPlayed()).toInstant().plus(registeredForDays().get(), ChronoUnit.DAYS);
		if (date.isAfter(Instant.now()))
			return date.toEpochMilli() - Instant.now().toEpochMilli();
		return -1;
	}

	public int getPlayTimeTotal(Player player) {
		return player.getStatistic(playtime.getKey());
	}

	/**
	 * Returns hours
	 */
	public double getPlayTime(Player player) {
		double pt = playedHours().get() - (double) getPlayTimeTotal(player) / playtime.getValue();
		if (pt < 0) return -1;
		return pt;
	}

}
