package buttondevteam.lib.player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import buttondevteam.lib.TBMCCoreAPI;

public abstract class ChromaGamerBase implements AutoCloseable {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players/";

	private static final HashMap<Class<?>, String> playerTypes = new HashMap<>();

	public static Map<Class<?>, String> getPlayerTypes() {
		return Collections.unmodifiableMap(playerTypes);
	}

	public static <T extends ChromaGamerBase> void addPlayerType(Class<T> cl, String folder) {
		playerTypes.put(cl, folder);
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, use MC UUIDs, for Discord data, use Discord IDs, etc.
	 */
	public abstract String getFileName();

	/**
	 * This method returns the folder the file is in. For example, for Minecraft data, this should be "minecraft", for Discord, "discord", etc.
	 */
	public abstract String getFolder();

	protected YamlConfiguration plugindata;

	public YamlConfiguration getData() {
		return plugindata;
	}

	// protected void load() {
	/*
	 * public static void load() { try { plugindata = YamlConfiguration.loadConfiguration(new File(getFolder(), getFileName())); } catch (Exception e) {
	 * TBMCCoreAPI.SendException("An error occured while loading gamer data", e); } } protected void save() { try { plugindata.save(new File(getFolder(), getFileName())); } catch (Exception e) {
	 * TBMCCoreAPI.SendException("An error occured while saving gamer data", e); } }
	 */

	protected static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		try {
			T obj = cl.newInstance();
			obj.plugindata = YamlConfiguration // TODO: Put all IDs
					.loadConfiguration(new File(TBMC_PLAYERS_DIR + playerTypes.get(cl), fname));
			return obj;
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while loading a " + cl.getSimpleName() + "!", e);
		}
		return null;
	}

	@Override
	public void close() throws Exception {
		plugindata.save(new File(TBMC_PLAYERS_DIR + getFolder(), getFileName()));
	}
}
