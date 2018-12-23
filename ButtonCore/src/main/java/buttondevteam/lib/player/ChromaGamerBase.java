package buttondevteam.lib.player;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import com.google.common.collect.HashBiMap;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@ChromaGamerEnforcer
public abstract class ChromaGamerBase implements AutoCloseable {
	public static final String TBMC_PLAYERS_DIR = "TBMC/players/";

	private static final HashBiMap<Class<? extends ChromaGamerBase>, String> playerTypes = HashBiMap.create();

	/**
	 * Used for connecting with every type of user ({@link #connectWith(ChromaGamerBase)})
	 */
	public static void RegisterPluginUserClass(Class<? extends ChromaGamerBase> userclass) {
		if (userclass.isAnnotationPresent(UserClass.class))
			playerTypes.put(userclass, userclass.getAnnotation(UserClass.class).foldername());
		else if (userclass.isAnnotationPresent(AbstractUserClass.class))
			playerTypes.put(userclass.getAnnotation(AbstractUserClass.class).prototype(),
					userclass.getAnnotation(AbstractUserClass.class).foldername());
		else // <-- Really important
			throw new RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase");
	}

	/**
	 * Returns the folder name for the given player class.
	 * 
	 * @param cl
	 *            The class to get the folder from (like {@link TBMCPlayerBase} or one of it's subclasses)
	 * @return The folder name for the given type
	 * @throws RuntimeException
	 *             If the class doesn't have the {@link UserClass} annotation.
	 */
	public static <T extends ChromaGamerBase> String getFolderForType(Class<T> cl) {
		if (cl.isAnnotationPresent(UserClass.class))
			return cl.getAnnotation(UserClass.class).foldername();
		else if (cl.isAnnotationPresent(AbstractUserClass.class))
			return cl.getAnnotation(AbstractUserClass.class).foldername();
		throw new RuntimeException("Class not registered as a user class! Use @UserClass or @AbstractUserClass");
	}

	/**
	 * Returns the player class for the given folder name.
	 * 
	 * @param foldername
	 *            The folder to get the class from (like "minecraft")
	 * @return The type for the given folder name or null if not found
	 */
	public static Class<? extends ChromaGamerBase> getTypeForFolder(String foldername) {
		return playerTypes.inverse().get(foldername);
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, MC UUIDs, for Discord data, use Discord IDs, etc.<br>
	 * <b>Does not include .yml</b>
	 */
	public final String getFileName() {
		return plugindata.getString(getFolder() + "_id");
	}

	/**
     * Use {@link #data(Object)} or {@link #data(String, Object)} where possible; the 'id' must be always set
	 */
	protected YamlConfiguration plugindata;

	/***
	 * Loads a user from disk and returns the user object. Make sure to use the subclasses' methods, where possible, like {@link TBMCPlayerBase#getPlayer(java.util.UUID, Class)}
	 *
     * @param fname Filename without .yml, usually UUID
     * @param cl User class
     * @return The user object
	 */
	public static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		try {
			T obj = cl.newInstance();
			final String folder = getFolderForType(cl);
			final File file = new File(TBMC_PLAYERS_DIR + folder, fname + ".yml");
			file.getParentFile().mkdirs();
			obj.plugindata = YamlConfiguration.loadConfiguration(file);
			obj.plugindata.set(folder + "_id", fname);
			return obj;
		} catch (Exception e) {
			TBMCCoreAPI.SendException("An error occured while loading a " + cl.getSimpleName() + "!", e);
		}
		return null;
	}

	private static ArrayList<Function<CommandSender, ? extends Optional<? extends ChromaGamerBase>>> senderConverters = new ArrayList<>();

	/**
	 * Adds a converter to the start of the list.
	 *
	 * @param converter The converter that returns an object corresponding to the sender or null, if it's not the right type.
	 */
	public static <T extends ChromaGamerBase> void addConverter(Function<CommandSender, Optional<T>> converter) {
		senderConverters.add(0, converter);
	}

	/**
	 * Get from the given sender. the object's type will depend on the sender's type. May be null, but shouldn't be.
	 *
	 * @param sender The sender to use
	 * @return A user as returned by a converter or null if none can supply it
	 */
	public static ChromaGamerBase getFromSender(CommandSender sender) {
		for (val converter : senderConverters) {
			val ocg = converter.apply(sender);
			if (ocg.isPresent())
				return ocg.get();
		}
		return null;
	}

	/**
	 * Saves the player. It'll pass all exceptions to the caller. To automatically handle the exception, use {@link #save()} instead.
	 */
	@Override
	public void close() throws Exception {
		if (plugindata.getKeys(false).size() > 0)
			plugindata.save(new File(TBMC_PLAYERS_DIR + getFolder(), getFileName() + ".yml"));
	}

	/**
	 * Saves the player. It'll handle all exceptions that may happen. To catch the exception, use {@link #close()} instead.
	 */
	public void save() {
		try {
			close();
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while saving player to " + getFolder() + "/" + getFileName() + ".yml!", e);
		}
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
		final String ownFolder = getFolder();
		final String userFolder = user.getFolder();
		if (ownFolder.equalsIgnoreCase(userFolder))
			throw new RuntimeException("Do not connect two accounts of the same type! Type: "+ownFolder);
		user.plugindata.set(ownFolder + "_id", plugindata.getString(ownFolder + "_id"));
		plugindata.set(userFolder + "_id", user.plugindata.getString(userFolder + "_id"));
		Consumer<YamlConfiguration> sync = sourcedata -> {
			final String sourcefolder = sourcedata == plugindata ? ownFolder : userFolder;
			final String id = sourcedata.getString(sourcefolder + "_id");
			for (val entry : playerTypes.entrySet()) { // Set our ID in all files we can find, both from our connections and the new ones
				if (entry.getKey() == getClass() || entry.getKey() == user.getClass())
					continue;
				final String otherid = sourcedata.getString(entry.getValue() + "_id");
				if (otherid == null)
					continue;
				try (ChromaGamerBase cg = getUser(otherid, entry.getKey())) {
					cg.plugindata.set(sourcefolder + "_id", id); // Set new IDs
					for (val item : playerTypes.entrySet())
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
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends ChromaGamerBase> T getAs(Class<T> cl) { // TODO: Provide a way to use TBMCPlayerBase's loaded players
		if (cl.getSimpleName().equals(getClass().getSimpleName()))
			return (T) this;
		String newfolder = getFolderForType(cl);
		if (newfolder == null)
			throw new RuntimeException("The specified class " + cl.getSimpleName() + " isn't registered!");
		if (newfolder.equals(getFolder())) // If in the same folder, the same filename is used
			return getUser(getFileName(), cl);
		if (!plugindata.contains(newfolder + "_id"))
			return null;
		return getUser(plugindata.getString(newfolder + "_id"), cl);
	}

	public String getFolder() {
		return getFolderForType(getClass());
	}

	private void ThrowIfNoUser() {
		if (!getClass().isAnnotationPresent(UserClass.class)
				&& !getClass().isAnnotationPresent(AbstractUserClass.class))
			throw new RuntimeException("Class not registered as a user class! Use @UserClass");
	}

	@SuppressWarnings("rawtypes")
    private final HashMap<String, PlayerData> datamap = new HashMap<>();

	/**
	 * Use from a data() method, which is in a method with the name of the key. For example, use flair() for the enclosing method of the outer data() to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T> PlayerData<T> data(String sectionname, T def) {
		ThrowIfNoUser();
		String mname = sectionname + "." + new Exception().getStackTrace()[2].getMethodName();
		if (!datamap.containsKey(mname))
			datamap.put(mname, new PlayerData<T>(mname, plugindata, def));
		return datamap.get(mname);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T> PlayerData<T> data(T def) {
		ThrowIfNoUser();
		String mname = new Exception().getStackTrace()[1].getMethodName();
		if (!datamap.containsKey(mname))
			datamap.put(mname, new PlayerData<T>(mname, plugindata, def));
		return datamap.get(mname);
	}

	@SuppressWarnings("rawtypes")
	private final HashMap<String, EnumPlayerData> dataenummap = new HashMap<>();
	private ChannelPlayerData datachannel;

	/**
	 * Use from a data() method, which is in a method with the name of the key. For example, use flair() for the enclosing method of the outer data() to save to and load from "flair"
	 * 
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(String sectionname, Class<T> cl, T def) {
		ThrowIfNoUser();
		String mname = sectionname + "." + new Exception().getStackTrace()[2].getMethodName();
		if (!dataenummap.containsKey(mname))
			dataenummap.put(mname, new EnumPlayerData<T>(mname, plugindata, cl, def));
		return dataenummap.get(mname);
	}

	/**
	 * Use from a method with the name of the key. For example, use flair() for the enclosing method to save to and load from "flair"
	 *
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> EnumPlayerData<T> dataEnum(Class<T> cl, T def) {
		ThrowIfNoUser();
		String mname = new Exception().getStackTrace()[1].getMethodName();
		if (!dataenummap.containsKey(mname))
			dataenummap.put(mname, new EnumPlayerData<T>(mname, plugindata, cl, def));
		return dataenummap.get(mname);
	}

	/**
	 * Channel
	 *
	 * @return A data object with methods to get and set
	 */
	@SuppressWarnings("unchecked")
	protected ChannelPlayerData dataChannel(Channel def) { //TODO: Make interface with fromString() method and require use of that for player data types
		ThrowIfNoUser();
		if (datachannel == null)
			datachannel = new ChannelPlayerData("channel", plugindata, def);
		return datachannel;
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

	//-----------------------------------------------------------------

	public ChannelPlayerData channel() {
		return dataChannel(Channel.GlobalChat);
	}
}
