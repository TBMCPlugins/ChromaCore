package buttondevteam.core.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.ResidentModes;
import com.palmergames.bukkit.towny.object.TownyUniverse;

/**
 * <p>
 * The class for holding data common to all TBMC plugins
 * </p>
 * <p>
 * Listen to the load and save events from this package to load and save
 * plugin-specific data
 * </p>
 * 
 * @author Norbi
 *
 */
public class TBMCPlayer {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players";

	public String PlayerName;

	public UUID UUID;

	public static HashMap<UUID, TBMCPlayer> OnlinePlayers = new HashMap<>();

	/**
	 * @param name
	 *            The player's name
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetFromName(String name) {
		@SuppressWarnings("deprecation")
		Player p = Bukkit.getPlayer(name);
		if (p != null)
			return GetPlayer(p); // TODO: Put playernames into filenames
		else
			return null;
	}

	/**
	 * @param p
	 *            The Player object
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetPlayer(Player p) {
		return TBMCPlayer.OnlinePlayers.get(p.getUniqueId());
	}

	static TBMCPlayer LoadPlayer(Player p) {
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
			if (!p.getName().equals(player.PlayerName)) {
				System.out.println("Renaming " + player.PlayerName + " to " + p.getName());
				TownyUniverse tu = Towny.getPlugin(Towny.class).getTownyUniverse();
				tu.getResidentMap().get(player.PlayerName).setName(p.getName());
				System.out.println("Renaming done.");
			}

			// Load in other plugins
			Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(yc, player));
			return player;
		}
	}

	static TBMCPlayer AddPlayer(Player p) {
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
}
