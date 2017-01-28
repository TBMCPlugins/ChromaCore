package buttondevteam.lib.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.configuration.file.YamlConfiguration;

import buttondevteam.lib.TBMCCoreAPI;

public abstract class ChromaGamerBase implements AutoCloseable {
	public static final String TBMC_PLAYERS_DIR = "TBMC/players/";

	private static final HashMap<Class<?>, String> playerTypes = new HashMap<>();

	/**
	 * Use only if outside Minecraft, and use it to register your plugin's class.
	 * 
	 * @param cl
	 *            The custom player class
	 * @param folder
	 *            The folder to store the data in (like "discord")
	 */
	public static <T extends ChromaGamerBase> void addPlayerType(Class<T> cl, String folder) {
		playerTypes.put(cl, folder);
	}

	/**
	 * Returns the folder name for the given player class. If a direct match is not found, it'll look for superclasses.
	 * 
	 * @param cl
	 *            The class to get the folder from (like {@link TBMCPlayerBase} or one of it's subclasses
	 * @return The folder name for the given type
	 */
	public static <T extends ChromaGamerBase> String getFolderForType(Class<T> cl) {
		if (playerTypes.containsKey(cl))
			return playerTypes.get(cl);
		return playerTypes.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(cl)).findAny()
				.orElseThrow(() -> new RuntimeException("Type not registered as a player type!")).getValue();
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, use MC UUIDs, for Discord data, use Discord IDs, etc.<br>
	 * <b>Does not include .yml</b>
	 */
	public abstract String getFileName();

	/**
	 * The 'id' must be always set
	 */
	protected YamlConfiguration plugindata;

	public String getID() {
		return plugindata != null ? plugindata.getString("id") : null;
	}

	protected static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		try {
			T obj = cl.newInstance();
			final File file = new File(TBMC_PLAYERS_DIR + getFolderForType(cl), fname + ".yml");
			file.mkdirs();
			obj.plugindata = YamlConfiguration.loadConfiguration(file);
			return obj;
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while loading a " + cl.getSimpleName() + "!", e);
		}
		return null;
	}

	/**
	 * Saves the player. It'll pass all exceptions to the caller. To automatically handle the exception, use {@link #save()} instead.
	 */
	@Override
	public void close() throws Exception {
		save_();
	}

	/**
	 * Saves the player. It'll send all exceptions that may happen. To catch the exception, use {@link #close()} instead.
	 */
	public void save() {
		try {
			save_();
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while saving player to " + getFolder() + "/" + getFileName() + ".yml!", e);
		}
	}

	private void save_() throws IOException {
		plugindata.save(new File(TBMC_PLAYERS_DIR + getFolder(), getFileName() + ".yml"));
	}

	/**
	 * Connect two accounts. Do not use for connecting two Minecraft accounts or similar. Also make sure you have the "id" tag set
	 * 
	 * @param user
	 *            The account to connect with
	 */
	public <T extends ChromaGamerBase> void connectWith(T user) {
		// Set the ID, go through all linked files and connect them as well
		plugindata.set(user.getFolder() + "_id", user.plugindata.getString("id"));
		final String ownFolder = user.getFolder();
		user.plugindata.set(ownFolder + "_id", plugindata.getString("id"));
		Consumer<YamlConfiguration> sync = sourcedata -> {
			final String sourcefolder = sourcedata == plugindata ? ownFolder : user.getFolder();
			final String id = sourcedata.getString("id");
			for (Entry<Class<?>, String> entry : playerTypes.entrySet()) { // Set our ID in all files we can find, both from our connections and the new ones
				final String otherid = sourcedata.getString(entry.getValue() + "_id");
				if (otherid == null)
					continue;
				try (@SuppressWarnings("unchecked")
				ChromaGamerBase cg = getUser(otherid, (Class<T>) entry.getKey())) {
					cg.plugindata.set(sourcefolder + "_id", id); // Set new IDs
					for (Entry<Class<?>, String> item : playerTypes.entrySet())
						if (sourcedata.contains(item.getValue() + "_id"))
							cg.plugindata.set(item.getValue() + "_id", sourcedata.getString(item.getValue() + "_id")); // Set all existing IDs
				} catch (Exception e) {
					TBMCCoreAPI.SendException("Failed to update " + sourcefolder + " ID in player files for " + id
							+ " in folder with " + entry.getValue() + " id " + otherid + "!", e);
				}
			}
		};
		sync.accept(plugindata);
		sync.accept(user.plugindata);
	}

	/**
	 * Returns this player as a plugin player. This will return a new instance unless the player is online.<br>
	 * Make sure to close both the returned and this object. A try-with-resources block or two can help.<br>
	 * 
	 * @param cl
	 *            The target player class
	 * @return The player as a {@link T} object or null if not having an account there
	 */
	public <T extends ChromaGamerBase> T getAs(Class<T> cl) {
		String newfolder = getFolderForType(cl);
		if (newfolder == null)
			throw new RuntimeException("The specified class " + cl.getSimpleName() + " isn't registered!");
		if (!plugindata.contains(newfolder + "_id"))
			return null;
		return getUser(plugindata.getString(newfolder + "_id"), cl);
	}

	public String getFolder() {
		return getFolderForType(getClass());
	}
}
