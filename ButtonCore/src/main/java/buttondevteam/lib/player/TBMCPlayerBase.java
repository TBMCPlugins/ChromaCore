package buttondevteam.lib.player;

import buttondevteam.lib.TBMCCoreAPI;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AbstractUserClass(foldername = "minecraft", prototype = TBMCPlayer.class)
@TBMCPlayerEnforcer
public abstract class TBMCPlayerBase extends ChromaGamerBase {
	protected UUID uuid;

	private String pluginname;

	protected TBMCPlayerBase() {
		if (getClass().isAnnotationPresent(PlayerClass.class))
			pluginname = getClass().getAnnotation(PlayerClass.class).pluginname();
		else
			throw new RuntimeException("Class not defined as player class! Use @PlayerClass");
	}

	public UUID getUUID() {
		if (uuid == null)
			uuid = UUID.fromString(getFileName());
		return uuid;
	}

	public PlayerData<String> PlayerName() {
		return super.data(null);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@Override
	protected <T> PlayerData<T> data(T def) {
		return super.data(pluginname, def);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@Override
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(Class<T> cl, T def) {
		return super.dataEnum(pluginname, cl, def);
	}

	/**
	 * Get player as a plugin player
	 * 
	 * @param uuid
	 *            The UUID of the player to get
	 * @param cl
	 *            The type of the player
	 * @return The requested player object
	 */
	@SuppressWarnings("unchecked")
	public static <T extends TBMCPlayerBase> T getPlayer(UUID uuid, Class<T> cl) {
		if (playermap.containsKey(uuid + "-" + cl.getSimpleName()))
			return (T) playermap.get(uuid + "-" + cl.getSimpleName());
		try {
			T player;
			if (playermap.containsKey(uuid + "-" + TBMCPlayer.class.getSimpleName())) {
				player = cl.newInstance();
				player.plugindata = playermap.get(uuid + "-" + TBMCPlayer.class.getSimpleName()).plugindata;
				playermap.put(uuid + "-" + cl.getSimpleName(), player); // It will get removed on player quit
			} else
				player = ChromaGamerBase.getUser(uuid.toString(), cl);
			player.uuid = uuid;
			return player;
		} catch (Exception e) {
			TBMCCoreAPI.SendException(
					"Failed to get player with UUID " + uuid + " and class " + cl.getSimpleName() + "!", e);
			return null;
		}
	}

	/**
	 * Key: UUID-Class
	 */
	static final ConcurrentHashMap<String, TBMCPlayerBase> playermap = new ConcurrentHashMap<>();

	/**
	 * Gets the TBMCPlayer object as a specific plugin player, keeping it's data<br>
	 * Make sure to use try-with-resources with this to save the data, as it may need to load the file
	 * 
	 * @param cl
	 *            The TBMCPlayer subclass
	 */
	public <T extends TBMCPlayerBase> T asPluginPlayer(Class<T> cl) {
		return getPlayer(uuid, cl);
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void savePlayer(TBMCPlayerBase player) {
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerSaveEvent(player));
		try {
			player.close();
		} catch (Exception e) {
			new Exception("Failed to save player data for " + player.PlayerName().get(), e).printStackTrace();
		}
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void joinPlayer(Player p) {
		TBMCPlayer player = TBMCPlayerBase.getPlayer(p.getUniqueId(), TBMCPlayer.class);
		Bukkit.getLogger().info("Loaded player: " + player.PlayerName().get());
		if (player.PlayerName().get() == null) {
			player.PlayerName().set(p.getName());
			Bukkit.getLogger().info("Player name saved: " + player.PlayerName().get());
		} else if (!p.getName().equals(player.PlayerName().get())) {
			Bukkit.getLogger().info("Renaming " + player.PlayerName().get() + " to " + p.getName());
			TownyUniverse tu = Towny.getPlugin(Towny.class).getTownyUniverse();
			Resident resident = tu.getResidentMap().get(player.PlayerName().get());
			if (resident == null) {
				Bukkit.getLogger().warning("Resident not found - couldn't rename in Towny.");
				TBMCCoreAPI.sendDebugMessage("Resident not found - couldn't rename in Towny.");
			} else if (tu.getResidentMap().contains(p.getName())) {
				Bukkit.getLogger().warning("Target resident name is already in use."); // TODO: Handle
				TBMCCoreAPI.sendDebugMessage("Target resident name is already in use.");
			} else
				try {
					TownyUniverse.getDataSource().renamePlayer(resident, p.getName());
				} catch (AlreadyRegisteredException e) {
					TBMCCoreAPI.SendException("Failed to rename resident, there's already one with this name.", e);
				} catch (NotRegisteredException e) {
					TBMCCoreAPI.SendException("Failed to rename resident, the resident isn't registered.", e);
				}
			player.PlayerName().set(p.getName());
			Bukkit.getLogger().info("Renaming done.");
		}
		playermap.put(p.getUniqueId() + "-" + TBMCPlayer.class.getSimpleName(), player);

		// Load in other plugins
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(player));
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player, p));
		player.save();
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void quitPlayer(Player p) {
		final TBMCPlayerBase player = playermap.get(p.getUniqueId() + "-" + TBMCPlayer.class.getSimpleName());
		player.save();
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player, p));
		Iterator<Entry<String, TBMCPlayerBase>> it = playermap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TBMCPlayerBase> entry = it.next();
			if (entry.getKey().startsWith(p.getUniqueId().toString()))
				it.remove();
		}
	}

	public static void savePlayers() {
		playermap.values().stream().forEach(p -> {
			try {
				p.close();
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Error while saving player " + p.PlayerName().get() + " (" + p.getFolder()
						+ "/" + p.getFileName() + ")!", e);
			}
		});
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
	 * @param name
	 *            The player's name
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
	public void close() throws Exception {
		Set<String> keys = plugindata.getKeys(false);
		if (keys.size() > 1) // PlayerName is always saved, but we don't need a file for just that
			super.close();
	}
}
