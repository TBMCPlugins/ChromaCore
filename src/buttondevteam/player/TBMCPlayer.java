package buttondevteam.player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TBMCPlayer {
	public String PlayerName;

	public UUID UUID;

	public static HashMap<UUID, TBMCPlayer> OnlinePlayers = new HashMap<>();

	public HashMap<String, String> Settings = new HashMap<>();

	public static TBMCPlayer AddPlayerIfNeeded(UUID uuid) {
		if (!AllPlayers.containsKey(uuid)) {
			TBMCPlayer player = new TBMCPlayer();
			player.UUID = uuid;
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				player.PlayerName = p.getName();
			AllPlayers.put(uuid, player);
			return player;
		}
		return AllPlayers.get(uuid);
	}

	public static void Load(YamlConfiguration yc) { //OLD
		ConfigurationSection cs = yc.getConfigurationSection("players");
		for (String key : cs.getKeys(false)) {
			ConfigurationSection cs2 = cs.getConfigurationSection(key);
			TBMCPlayer mp = AddPlayerIfNeeded(java.util.UUID.fromString(cs2.getString("uuid")));
		}
	}

	public static void Save(YamlConfiguration yc) { //OLD
		ConfigurationSection cs = yc.createSection("players");
		for (TBMCPlayer mp : TBMCPlayer.AllPlayers.values()) {
			ConfigurationSection cs2 = cs.createSection(mp.UUID.toString());
		}
	}

	/**
	 * @param name
	 *            The player's name
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetFromName(String name) {
		@SuppressWarnings("deprecation")
		Player p = Bukkit.getPlayer(name);
		if (p != null)
			return AllPlayers.get(p.getUniqueId());
		else
			return null;
	}

	/**
	 * @param p
	 *            The Player object
	 * @return The {@link TBMCPlayer} object for the player
	 */
	public static TBMCPlayer GetPlayer(Player p) {
		return TBMCPlayer.AllPlayers.get(p.getUniqueId());
	}

	static TBMCPlayer LoadPlayer(UUID uuid) throws Exception {
		if (OnlinePlayers.containsKey(uuid))
			return OnlinePlayers.get(uuid);
		File file = new File("TBMC/players");
		file.mkdirs();
		file = new File("TBMC/players", uuid.toString() + ".yml");
		if (!file.exists())
			return AddPlayer(uuid);
		else {
			final YamlConfiguration yc = new YamlConfiguration();
			yc.load(file);
			
		}
	}

	static TBMCPlayer AddPlayer(UUID uuid) {
		if (OnlinePlayers.containsKey(uuid))
			return OnlinePlayers.get(uuid);
		TBMCPlayer player = new TBMCPlayer();
		player.UUID = uuid;
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			player.PlayerName = p.getName();
		AllPlayers.put(uuid, player);
		return player;
	}
}
