package buttondevteam.lib.player;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import buttondevteam.lib.TBMCCoreAPI;

public abstract class TBMCPlayerBase extends ChromaGamerBase {
	private static final String FOLDER_NAME = "minecraft";
	protected UUID uuid;

	public UUID getUUID() {
		return uuid;
	}

	public String getPlayerName() {
		return plugindata.getString("playername", "");
	}

	public void setPlayerName(String value) {
		plugindata.set("playername", value);
	}

	@Override
	public String getFileName() {
		return getUUID().toString();
	}

	// protected ConfigurationSection plugindata;

	/*
	 * public static void load() { super.load(); plugindata = super.plugindata.getConfigurationSection(getPluginName()); if (plugindata == null) plugindata =
	 * super.plugindata.createSection(getPluginName()); } protected void save() { plugindata = super.plugindata.createSection(getPluginName(), plugindata.getValues(true)); super.save(); }
	 */

	static {
		addPlayerType(TBMCPlayerBase.class, FOLDER_NAME);
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
			if (playerfiles.containsKey(uuid)) {
				player = cl.newInstance();
				player.plugindata = playerfiles.get(uuid);
				playermap.put(player.uuid + "-" + player.getFolder(), player); // It will get removed on player quit
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

	private static final ConcurrentHashMap<UUID, YamlConfiguration> playerfiles = new ConcurrentHashMap<>();

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
			new Exception("Failed to save player data for " + player.getPlayerName(), e).printStackTrace();
		}
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void joinPlayer(UUID uuid) {
		YamlConfiguration yc;
		if (playerfiles.containsKey(uuid))
			yc = playerfiles.get(uuid);
		else
			playerfiles.put(uuid, yc = YamlConfiguration.loadConfiguration(new File("minecraft", uuid + ".yml")));
		/*Bukkit.getLogger().info("Loaded player: " + player.getPlayerName());
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
				try {
					TownyUniverse.getDataSource().renamePlayer(resident, p.getName());
				} catch (AlreadyRegisteredException e) {
					TBMCCoreAPI.SendException("Failed to rename resident, there's already one with this name.", e);
				} catch (NotRegisteredException e) {
					TBMCCoreAPI.SendException("Failed to rename resident, the resident isn't registered.", e);
				}
			player.setPlayerName(p.getName());
			Bukkit.getLogger().info("Renaming done.");
		}

		// Load in other plugins
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(player));
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player));*/
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void quitPlayer(Player p) {
		Iterator<Entry<String, TBMCPlayerBase>> it = playermap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TBMCPlayerBase> entry = it.next();
			if (entry.getKey().startsWith(p.getUniqueId().toString())) { // Save every player data
				TBMCPlayerBase player = entry.getValue(); // TODO: Separate plugin data by plugin name (annotations?)
				Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player));
			}
		}
		final YamlConfiguration playerfile = playerfiles.get(p.getUniqueId());
		try { // Only save to file once, not for each plugin
			playerfile.save(p.getUniqueId().toString() + ".yml"); // TODO: Bring this together with the close() method, like a common method or something
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while saving quitting player " + playerfile.getString("playername") + " ("
					+ "minecraft/" + p.getUniqueId() + ".yml)!", e);
		}
	}

	public static void savePlayers() {
		playermap.values().stream().forEach(p -> {
			try {
				p.close();
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Error while saving player " + p.getPlayerName() + " (" + p.getFolder() + "/"
						+ p.getFileName() + ")!", e);
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

	/**
	 * Get player information. This method calls the {@link TBMCPlayerGetInfoEvent} to get all the player information across the TBMC plugins.
	 * 
	 * @param target
	 *            The {@link InfoTarget} to return the info for.
	 * @return The player information.
	 */
	public String getInfo(InfoTarget target) {
		TBMCPlayerGetInfoEvent event = new TBMCPlayerGetInfoEvent(this, target);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getResult();
	}

	public enum InfoTarget {
		MCHover, MCCommand, Discord
	}
}