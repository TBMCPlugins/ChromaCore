package buttondevteam.lib.player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.configuration.file.YamlConfiguration;

import buttondevteam.lib.TBMCCoreAPI;

public abstract class ChromaGamerBase implements AutoCloseable {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players/";

	private static final HashMap<Class<?>, String> playerTypes = new HashMap<>();

	public static <T extends ChromaGamerBase> void addPlayerType(Class<T> cl, String folder) {
		playerTypes.put(cl, folder);
	}

	public static <T extends ChromaGamerBase> String getFolderForType(Class<T> cl) {
		return playerTypes.get(cl);
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, use MC UUIDs, for Discord data, use Discord IDs, etc.
	 */
	public abstract String getFileName();

	/**
	 * The 'id' must be always set
	 */
	protected YamlConfiguration plugindata;

	public YamlConfiguration getData() {
		return plugindata;
	}

	public String getID() {
		return plugindata != null ? plugindata.getString("id") : null;
	}

	protected static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		try {
			T obj = cl.newInstance();
			obj.plugindata = YamlConfiguration
					.loadConfiguration(new File(TBMC_PLAYERS_DIR + playerTypes.get(cl), fname));
			return obj;
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while loading a " + cl.getSimpleName() + "!", e);
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		plugindata.save(new File(TBMC_PLAYERS_DIR + getFolderForType(getClass()), getFileName()));
	}

	public <T extends ChromaGamerBase> void connectWith(T user) {
		// Set the ID, go through all linked files and connect them as well
		plugindata.set(playerTypes.get(user.getClass()) + "_id", user.plugindata.getString("id"));
		final String ownFolder = getFolderForType(getClass());
		user.plugindata.set(ownFolder + "_id", plugindata.getString("id"));
		BiConsumer<YamlConfiguration, YamlConfiguration> sync = (pdata1, pdata2) -> {
			for (Entry<Class<?>, String> entry : playerTypes.entrySet())
				if (pdata1.contains(entry.getValue() + "_id", false))
					pdata2.set(entry.getValue() + "_id", pdata1.getString(entry.getValue() + "_id"));
		}; // ...
	}
}
