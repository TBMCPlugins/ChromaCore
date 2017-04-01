package buttondevteam.lib.player;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import buttondevteam.lib.TBMCCoreAPI;

@UserClass(foldername = "minecraft")
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
		return uuid;
	}

	public PlayerData<String> PlayerName() {
		//System.out.println("Calling playername"); // TODO: TMP - The data will only get stored if it's changed
		return super.data();
	}

	@Override
	public String getFileName() {
		return getUUID().toString();
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@Override
	protected <T> PlayerData<T> data() {
		//System.out.println("Calling TMBCPlayerBase data"); // TODO: TMP - Sigh
		return super.data(pluginname);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@Override
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(Class<T> cl) {
		return super.dataEnum(pluginname, cl);
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
		//System.out.println("A");
		try {
			T player;
			if (playermap.containsKey(uuid + "-" + TBMCPlayer.class.getSimpleName())) {
				//System.out.println("B"); - Don't program when tired
				player = cl.newInstance();
				player.plugindata = playermap.get(uuid + "-" + TBMCPlayer.class.getSimpleName()).plugindata;
				playermap.put(uuid + "-" + cl.getSimpleName(), player); // It will get removed on player quit
			} else
				player = ChromaGamerBase.getUser(uuid.toString(), cl);
			//System.out.println("C");
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
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player));
		player.save();
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void quitPlayer(Player p) {
		final TBMCPlayerBase player = playermap.get(p.getUniqueId() + "-" + TBMCPlayer.class.getSimpleName());
		player.save();
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player));
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
}
