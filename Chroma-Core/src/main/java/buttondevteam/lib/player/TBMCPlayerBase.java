package buttondevteam.lib.player;

import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

@AbstractUserClass(foldername = "minecraft", prototype = TBMCPlayer.class)
@TBMCPlayerEnforcer
public abstract class TBMCPlayerBase extends ChromaGamerBase {
	protected UUID uuid;

	@Getter
	private final IHaveConfig config = new IHaveConfig(this::save);

	public UUID getUUID() {
		if (uuid == null)
			uuid = UUID.fromString(getFileName());
		return uuid;
	}

	public ConfigData<String> PlayerName() {
		return super.config.getData("PlayerName", "");
	}

	/**
	 * Get player as a plugin player.
	 *
	 * @param uuid The UUID of the player to get
	 * @param cl   The type of the player
	 * @return The requested player object
	 */
	public static <T extends TBMCPlayerBase> T getPlayer(UUID uuid, Class<T> cl) {
		var player = ChromaGamerBase.getUser(uuid.toString(), cl);
		if (!player.getUUID().equals(uuid)) //It will be set from the filename because we check it for scheduling the uncache.
			throw new IllegalStateException("Player UUID differs after converting from and to string...");
		return player;
	}

	@Override
	public void init() {
		super.init();

		String pluginname;
		if (getClass().isAnnotationPresent(PlayerClass.class))
			pluginname = getClass().getAnnotation(PlayerClass.class).pluginname();
		else
			throw new RuntimeException("Class not defined as player class! Use @PlayerClass");

		var playerData = commonUserData.getPlayerData();
		var section = playerData.getConfigurationSection(pluginname);
		if (section == null) section = playerData.createSection(pluginname);
		config.reset(section);
	}

	@Override
	protected void scheduleUncache() { //Don't schedule it, it will happen on quit - if the player is online
		var p = Bukkit.getPlayer(getUUID());
		if (p == null || !p.isOnline())
			super.scheduleUncache();
	}

	/**
	 * This method returns a TBMC player from their name. See {@link Bukkit#getOfflinePlayer(String)}.
	 *
	 * @param name The player's name
	 * @return The {@link TBMCPlayer} object for the player
	 */
	@SuppressWarnings("deprecation")
	public static <T extends TBMCPlayerBase> T getFromName(String name, Class<T> cl) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return getPlayer(p.getUniqueId(), cl);
	}

	@Override
	protected void save() {
		Set<String> keys = commonUserData.getPlayerData().getKeys(false);
		if (keys.size() > 1) // PlayerName is always saved, but we don't need a file for just that
			super.save();
	}
}
