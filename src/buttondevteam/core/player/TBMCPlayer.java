package buttondevteam.core.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TBMCPlayer {
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
			TBMCPlayer player = new TBMCPlayer();
			player.UUID = uuid;
			player.PlayerName = yc.getString("playername");

			// Load in other plugins
			Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerLoadEvent(yc, player));
			return player;
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
		OnlinePlayers.put(uuid, player);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerAddEvent(player));
		SavePlayer(player);
		return player;
	}

	static void SavePlayer(TBMCPlayer player) {
		YamlConfiguration yc = new YamlConfiguration();
		yc.set("playername", player.PlayerName);
		Bukkit.getServer().getPluginManager().callEvent(new TBMCPlayerSaveEvent(yc, player));
		try {
			yc.save("tbmcplayers/" + player.UUID + ".yml");
		} catch (IOException e) {
			new Exception("Failed to save player data for " + player.PlayerName, e).printStackTrace();
		}
	}
}
