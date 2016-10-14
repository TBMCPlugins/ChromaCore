package buttondevteam.bucket.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
 * Listen to the load and save events from this package to load and save plugin-specific data
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCPlayer {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players";

	public String PlayerName;

	public UUID UUID;

	public <T extends TBMCPlayer> T AsPluginPlayer(Class<T> cl) {
		T obj = null;
		try {
			obj = cl.newInstance();
			obj.UUID = UUID;
			obj.PlayerName = PlayerName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	public static HashMap<UUID, TBMCPlayer> OnlinePlayers = new HashMap<>();

	/**
	 * @param name
	 *            The player's name
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetFromName(String name) {
		@SuppressWarnings("deprecation")
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		if (p != null)
			return GetPlayer(p);
		else
			return null;
	}

	/**
	 * @param p
	 *            The Player object
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetPlayer(OfflinePlayer p) {
		if (TBMCPlayer.OnlinePlayers.containsKey(p.getUniqueId()))
			return TBMCPlayer.OnlinePlayers.get(p.getUniqueId());
		else
			return TBMCPlayer.LoadPlayer(p);
	}

	protected static TBMCPlayer LoadPlayer(OfflinePlayer p) {
		if (OnlinePlayers.containsKey(p.getUniqueId()))
			return OnlinePlayers.get(p.getUniqueId());
		File file = new File(TBMC_PLAYERS_DIR);
		file.mkdirs();
		file = new File(TBMC_PLAYERS_DIR, p.getUniqueId().toString() + ".yml");
		if (!file.exists())
			return AddPlayer(p);
		else {
			final YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(file);
			} catch (Exception e) {
				new Exception("Failed to load player data for " + p.getUniqueId(), e).printStackTrace();
				return null;
			}
			TBMCPlayer player = new TBMCPlayer();
			player.UUID = p.getUniqueId();
			player.PlayerName = yc.getString("playername");
			System.out.println("Player name: " + player.PlayerName);
			if (player.PlayerName == null) {
				player.PlayerName = p.getName();
				System.out.println("Player name saved: " + player.PlayerName);
			} else if (!p.getName().equals(player.PlayerName)) {
				System.out.println("Renaming " + player.PlayerName + " to " + p.getName());
				TownyUniverse tu = Towny.getPlugin(Towny.class).getTownyUniverse();
				Resident resident = tu.getResidentMap().get(player.PlayerName);
				if (resident == null)
					System.out.println("Resident not found - couldn't rename in Towny.");
				else if (tu.getResidentMap().contains(p.getName()))
					System.out.println("Target resident name is already in use."); // TODO: Handle
				else
					resident.setName(p.getName());
				player.PlayerName = p.getName();
				System.out.println("Renaming done.");
			}

			// Load in other plugins
			Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(yc, player));
			return player;
		}
	}

	static TBMCPlayer AddPlayer(OfflinePlayer p) {
		TBMCPlayer player = new TBMCPlayer();
		player.UUID = p.getUniqueId();
		player.PlayerName = p.getName();
		OnlinePlayers.put(p.getUniqueId(), player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerAddEvent(player));
		SavePlayer(player);
		return player;
	}

	static void SavePlayer(TBMCPlayer player) {
		YamlConfiguration yc = new YamlConfiguration();
		yc.set("playername", player.PlayerName);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerSaveEvent(yc, player));
		try {
			yc.save(TBMC_PLAYERS_DIR + "/" + player.UUID + ".yml");
		} catch (IOException e) {
			new Exception("Failed to save player data for " + player.PlayerName, e).printStackTrace();
		}
	}

	static void JoinPlayer(TBMCPlayer player) {
		OnlinePlayers.put(player.UUID, player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerJoinEvent(player));
	}

	static void QuitPlayer(TBMCPlayer player) {
		OnlinePlayers.remove(player.UUID);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerQuitEvent(player));
	}

	<T extends TBMCPlayer> T GetAs(Class<T> cl) { // TODO: Serialize player classes
		return null;
	}
}
