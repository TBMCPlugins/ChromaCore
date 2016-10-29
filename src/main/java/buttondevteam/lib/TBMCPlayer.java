package buttondevteam.lib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

/**
 * <p>
 * The class for holding data common to all TBMC plugins
 * </p>
 * <p>
 * Use {@link #asPluginPlayer(Class)} to get plugin-specific data
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCPlayer implements AutoCloseable {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players";

	private HashMap<String, Object> data = new HashMap<>();

	/**
	 * <p>
	 * Gets a player data entry for the caller plugin returning the desired type.<br>
	 * <i>It will automatically determine the key and the return type.</i><br>
	 * Usage:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * public String getPlayerName() {
	 * 	return getData();
	 * }
	 * </pre>
	 * 
	 * @return The value or null if not found
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getData() {
		StackTraceElement st = new Exception().getStackTrace()[0];
		String mname = st.getMethodName();
		if (!mname.startsWith("get"))
			throw new UnsupportedOperationException("Can only use getData from a getXYZ method");
		return (T) LoadedPlayers.get(uuid).data.get(mname.substring("get".length()).toLowerCase());
	}

	/**
	 * Sets a player data entry <i>based on the caller method</i><br>
	 * Usage:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * public String setPlayerName(String value) {
	 * 	return setData(value);
	 * }
	 * </pre>
	 * 
	 * @param value
	 *            The value to set
	 */
	protected void setData(Object value) {
		StackTraceElement st = new Exception().getStackTrace()[0];
		String mname = st.getMethodName();
		if (!mname.startsWith("set"))
			throw new UnsupportedOperationException("Can only use setData from a setXYZ method");
		LoadedPlayers.get(uuid).data.put(mname.substring("set".length()).toLowerCase(), value);
	}

	/**
	 * Gets the player's Minecraft name
	 * 
	 * @return The player's Minecraft name
	 */
	public String getPlayerName() {
		return getData();
	}

	/**
	 * Sets the player's Minecraft name
	 * 
	 * @param playerName
	 *            the new name
	 */
	public void setPlayerName(String playerName) {
		setData(playerName);
	}

	private UUID uuid; // Do not save it in the file

	/**
	 * Get the player's UUID
	 * 
	 * @return The Minecraft UUID of the player
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Gets the TBMCPlayer object as a specific plugin player, keeping it's data
	 * 
	 * @param cl
	 * @return
	 */
	public <T extends TBMCPlayer> T asPluginPlayer(Class<T> cl) {
		T obj = null;
		try {
			obj = cl.newInstance();
			((TBMCPlayer) obj).data.putAll(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	private static HashMap<UUID, TBMCPlayer> LoadedPlayers = new HashMap<>();

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
	public static TBMCPlayer getFromName(String name) {
		@SuppressWarnings("deprecation")
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		if (p != null)
			return getPlayer(p);
		else
			return null;
	}

	/**
	 * This method returns a TBMC player from a Bukkit player. Calling this method may return an offline player, therefore it's highly recommended to use {@link #close()} to unload the player data.
	 * Using try-with-resources may be the easiest way to achieve this. Example:
	 * 
	 * <pre>
	 * {@code
	 * try(TBMCPlayer player = getPlayer(p))
	 * {
	 * 	...
	 * }
	 * </pre>
	 * 
	 * @param p
	 *            The Player object
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer getPlayer(OfflinePlayer p) {
		if (TBMCPlayer.LoadedPlayers.containsKey(p.getUniqueId()))
			return TBMCPlayer.LoadedPlayers.get(p.getUniqueId());
		else
			return TBMCPlayer.loadPlayer(p);
	}

	/**
	 * This method returns a TBMC player from a Bukkit player. Calling this method may return an offline player, therefore it's highly recommended to use {@link #close()} to unload the player data.
	 * Using try-with-resources may be the easiest way to achieve this. Example:
	 * 
	 * <pre>
	 * {@code
	 * try(TBMCPlayer player = getPlayer(p))
	 * {
	 * 	...
	 * }
	 * </pre>
	 * 
	 * @param p
	 *            The Player object
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer getPlayer(UUID uuid) {
		if (TBMCPlayer.LoadedPlayers.containsKey(uuid))
			return TBMCPlayer.LoadedPlayers.get(uuid);
		else
			return TBMCPlayer.loadPlayer(Bukkit.getOfflinePlayer(uuid));
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static TBMCPlayer loadPlayer(OfflinePlayer p) {
		if (LoadedPlayers.containsKey(p.getUniqueId()))
			return LoadedPlayers.get(p.getUniqueId());
		File file = new File(TBMC_PLAYERS_DIR);
		file.mkdirs();
		file = new File(TBMC_PLAYERS_DIR, p.getUniqueId().toString() + ".yml");
		if (!file.exists())
			return addPlayer(p);
		else {
			final YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(file);
			} catch (Exception e) {
				new Exception("Failed to load player data for " + p.getUniqueId(), e).printStackTrace();
				return null;
			}
			TBMCPlayer player = new TBMCPlayer();
			player.uuid = p.getUniqueId();
			player.data.putAll(yc.getValues(true));
			Bukkit.getLogger().info("Loaded player: " + player.getPlayerName());
			if (player.getPlayerName() == null) {
				player.setPlayerName(p.getName());
				Bukkit.getLogger().info("Player name saved: " + player.getPlayerName());
			} else if (!p.getName().equals(player.getPlayerName())) {
				Bukkit.getLogger().info("Renaming " + player.getPlayerName() + " to " + p.getName());
				TownyUniverse tu = Towny.getPlugin(Towny.class).getTownyUniverse();
				Resident resident = tu.getResidentMap().get(player.getPlayerName());
				if (resident == null)
					Bukkit.getLogger().warning("Resident not found - couldn't rename in Towny.");
				else if (tu.getResidentMap().contains(p.getName()))
					Bukkit.getLogger().warning("Target resident name is already in use."); // TODO: Handle
				else
					resident.setName(p.getName());
				player.setPlayerName(p.getName());
				Bukkit.getLogger().info("Renaming done.");
			}
			LoadedPlayers.put(p.getUniqueId(), player);

			// Load in other plugins
			Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(yc, player));
			return player;
		}
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static TBMCPlayer addPlayer(OfflinePlayer p) {
		TBMCPlayer player = new TBMCPlayer();
		player.uuid = p.getUniqueId();
		player.setPlayerName(p.getName());
		LoadedPlayers.put(p.getUniqueId(), player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerAddEvent(player));
		savePlayer(player);
		return player;
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void savePlayer(TBMCPlayer player) {
		YamlConfiguration yc = new YamlConfiguration();
		for (Entry<String, Object> item : player.data.entrySet())
			yc.set(item.getKey(), item.getValue());
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerSaveEvent(yc, player));
		try {
			yc.save(TBMC_PLAYERS_DIR + "/" + player.uuid + ".yml");
		} catch (IOException e) {
			new Exception("Failed to save player data for " + player.getPlayerName(), e).printStackTrace();
		}
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void joinPlayer(TBMCPlayer player) {
		LoadedPlayers.put(player.uuid, player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player));
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void quitPlayer(TBMCPlayer player) {
		LoadedPlayers.remove(player.uuid);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player));
	}

	/**
	 * By default the player data will only get cleaned from memory when the player quits. Therefore this method must be called when accessing an offline player to clean the player data up. Calling
	 * this method will have no effect on online players.<br>
	 * Therefore, the recommended use is to call it when using {@link #GetPlayer} or use try-with-resources.
	 */
	@Override
	public void close() throws Exception {
		if (!Bukkit.getPlayer(uuid).isOnline())
			LoadedPlayers.remove(uuid);
	}
}
