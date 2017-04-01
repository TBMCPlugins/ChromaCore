package buttondevteam.lib.player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import buttondevteam.lib.TBMCCoreAPI;

public abstract class ChromaGamerBase implements AutoCloseable {
	public static final String TBMC_PLAYERS_DIR = "TBMC/players/";

	private static final HashMap<Class<?>, String> playerTypes = new HashMap<>();

	/**
	 * Used for connecting with every type of user ({@link #connectWith(ChromaGamerBase)})
	 */
	public static void RegisterPluginUserClass(Class<? extends ChromaGamerBase> userclass) {
		if (userclass.isAnnotationPresent(UserClass.class))
			playerTypes.put(userclass, userclass.getAnnotation(UserClass.class).foldername());
		throw new RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase");
	}

	/**
	 * Returns the folder name for the given player class.
	 * 
	 * @param cl
	 *            The class to get the folder from (like {@link TBMCPlayerBase} or one of it's subclasses
	 * @return The folder name for the given type
	 * @throws RuntimeException
	 *             If the class doesn't have the {@link UserClass} annotation.
	 */
	public static <T extends ChromaGamerBase> String getFolderForType(Class<T> cl) {
		if (cl.isAnnotationPresent(UserClass.class))
			return cl.getAnnotation(UserClass.class).foldername();
		throw new RuntimeException("Class not registered as a user class! Use @UserClass");
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, use MC UUIDs, for Discord data, use Discord IDs, etc.<br>
	 * <b>Does not include .yml</b>
	 */
	public abstract String getFileName();

	/**
	 * Use {@link #data()} or {@link #data(String)} where possible; the 'id' must be always set
	 */
	protected YamlConfiguration plugindata;

	public String getID() {
		return plugindata != null ? plugindata.getString("id") : null;
	}

	/***
	 * Loads a user from disk and returns the user object. Make sure to use the subclasses' methods, where possible, like {@link TBMCPlayerBase#getPlayer(java.util.UUID, Class)}
	 * 
	 * @param fname
	 * @param cl
	 * @return
	 */
	public static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		try {
			T obj = cl.newInstance();
			final File file = new File(TBMC_PLAYERS_DIR + getFolderForType(cl), fname + ".yml");
			file.getParentFile().mkdirs();
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
		if (!playerTypes.containsKey(getClass()))
			throw new RuntimeException("Class not registered as a user class! Use TBMCCoreAPI.RegisterUserClass");
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
	 * Retunrs the ID for the T typed player object connected with this one or null if no connection found.
	 * 
	 * @param cl
	 *            The player class to get the ID from
	 * @return The ID or null if not found
	 */
	public <T extends ChromaGamerBase> String getConnectedID(Class<T> cl) {
		return plugindata.getString(getFolderForType(cl) + "_id");
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

	@SuppressWarnings("rawtypes")
	private HashMap<String, PlayerData> datamap = new HashMap<>();

	/**
	 * Use from a data() method, which is in a method with the name of the key. For example, use flair() for the enclosing method of the outer data() to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T> PlayerData<T> data(String sectionname) {
		if (!getClass().isAnnotationPresent(UserClass.class))
			throw new RuntimeException("Class not registered as a user class! Use @UserClass");
		String mname = sectionname + "." + new Exception().getStackTrace()[2].getMethodName();
		if (!datamap.containsKey(mname))
			datamap.put(mname, new PlayerData<T>(mname, plugindata));
		return datamap.get(mname);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T> PlayerData<T> data() {
		if (!getClass().isAnnotationPresent(UserClass.class))
			throw new RuntimeException("Class not registered as a user class! Use @UserClass");
		String mname = new Exception().getStackTrace()[1].getMethodName();
		if (!datamap.containsKey(mname))
			datamap.put(mname, new PlayerData<T>(mname, plugindata));
		return datamap.get(mname);
	}

	@SuppressWarnings("rawtypes")
	private HashMap<String, EnumPlayerData> dataenummap = new HashMap<>();

	/**
	 * Use from a data() method, which is in a method with the name of the key. For example, use flair() for the enclosing method of the outer data() to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(String sectionname, Class<T> cl) {
		if (!getClass().isAnnotationPresent(UserClass.class))
			throw new RuntimeException("Class not registered as a user class! Use @UserClass");
		String mname = sectionname + "." + new Exception().getStackTrace()[2].getMethodName();
		if (!dataenummap.containsKey(mname))
			dataenummap.put(mname, new EnumPlayerData<T>(mname, plugindata, cl));
		return dataenummap.get(mname);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(Class<T> cl) {
		if (!getClass().isAnnotationPresent(UserClass.class))
			throw new RuntimeException("Class not registered as a user class! Use @UserClass");
		String mname = new Exception().getStackTrace()[1].getMethodName();
		if (!dataenummap.containsKey(mname))
			dataenummap.put(mname, new EnumPlayerData<T>(mname, plugindata, cl));
		return dataenummap.get(mname);
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
