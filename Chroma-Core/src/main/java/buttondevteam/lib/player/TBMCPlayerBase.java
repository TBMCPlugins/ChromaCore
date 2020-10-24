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
		return config.getData("PlayerName", "");
	}

	/**
	 * Get player as a plugin player
	 *
	 * @param uuid The UUID of the player to get
	 * @param cl   The type of the player
	 * @return The requested player object
	 */
	public static <T extends TBMCPlayerBase> T getPlayer(UUID uuid, Class<T> cl) {
		var player = ChromaGamerBase.getUser(uuid.toString(), cl);
		if (player.uuid.equals(uuid))
			throw new IllegalStateException("Player UUID differs after converting from and to string...");
		return player;
	}

	@Override
	public void init() {
		super.init();
		uuid = UUID.fromString(getFileName());

		String pluginname;
		if (getClass().isAnnotationPresent(PlayerClass.class))
			pluginname = getClass().getAnnotation(PlayerClass.class).pluginname();
		else
			throw new RuntimeException("Class not defined as player class! Use @PlayerClass");

		var section = super.plugindata.getConfigurationSection(pluginname);
		if (section == null) section = super.plugindata.createSection(pluginname);
		config.reset(section);
	}

	@Override
	protected void scheduleUncache() { //Don't schedule it, it will happen on quit
	}

	/**
	 * This method returns a TBMC player from their name. Calling this method may return an offline player which will load it, therefore it's highly recommended to use {@link #close()} to unload the
	 * player data. Using try-with-resources may be the easiest way to achieve this. Example:
	 *
	 * <pre>
	 * {@code
	 * try(TBMCPlayer player = getFromName(p))
	 * {
	 * 	...
	 * }
	 * </pre>
	 *
	 * @param name The player's name
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static <T extends TBMCPlayerBase> T getFromName(String name, Class<T> cl) {
		@SuppressWarnings("deprecation")
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		if (p != null)
			return getPlayer(p.getUniqueId(), cl);
		else
			return null;
	}

	@Override
	protected void save() {
		Set<String> keys = plugindata.getKeys(false);
		if (keys.size() > 1) // PlayerName is always saved, but we don't need a file for just that
			super.save();
	}
}
