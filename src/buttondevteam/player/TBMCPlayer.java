package buttondevteam.player;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class TBMCPlayer {
	public String PlayerName;

	public UUID UUID;

	public static HashMap<UUID, TBMCPlayer> AllPlayers = new HashMap<>();

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

	public static void Load(YamlConfiguration yc) {
		ConfigurationSection cs = yc.getConfigurationSection("players");
		for (String key : cs.getKeys(false)) {
			ConfigurationSection cs2 = cs.getConfigurationSection(key);
			TBMCPlayer mp = AddPlayerIfNeeded(java.util.UUID
					.fromString(cs2.getString("uuid")));
		}
	}

	public static void Save(YamlConfiguration yc) {
		ConfigurationSection cs = yc.createSection("players");
		for (TBMCPlayer mp : TBMCPlayer.AllPlayers.values()) {
			ConfigurationSection cs2 = cs.createSection(mp.UUID.toString());
		}
	}

	public static TBMCPlayer GetFromName(String name) {
		Player p = Bukkit.getPlayer(name);
		if (p != null)
			return AllPlayers.get(p.getUniqueId());
		else
			return null;
	}

	public static TBMCPlayer GetFromPlayer(Player p) {
		return TBMCPlayer.AllPlayers.get(p.getUniqueId());
	}
}
