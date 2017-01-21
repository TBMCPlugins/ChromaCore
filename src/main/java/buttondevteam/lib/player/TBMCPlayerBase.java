package buttondevteam.lib.player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

	public abstract String getPluginName();

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
		T obj = ChromaGamerBase.getUser(uuid.toString(), cl);
		obj.uuid = uuid;
		return obj;
	}

	/**
	 * Key: UUID-Class
	 */
	static final ConcurrentHashMap<String, TBMCPlayerBase> playermap = new ConcurrentHashMap<>();

	/**
	 * Gets the TBMCPlayer object as a specific plugin player, keeping it's data *
	 * 
	 * @param p
	 *            Player to get
	 * @param cl
	 *            The TBMCPlayer subclass
	 */
	public <T extends TBMCPlayerBase> T asPluginPlayer(Class<T> cl) {
		return getPlayer(uuid, cl);
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static <T extends TBMCPlayerBase> T loadPlayer(OfflinePlayer p, Class<T> cl) {
		T player = getPlayer(p.getUniqueId(), cl);
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
		return player;
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
	public static void joinPlayer(TBMCPlayerBase player) {
		playermap.put(player.uuid + "-" + player.getFolder(), player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player));
	}

	/**
	 * Only intended to use from ButtonCore
	 */
	public static void quitPlayer(TBMCPlayerBase player) {
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player));
		playermap.remove(player.uuid + "-" + player.getFolder());
	}
}
