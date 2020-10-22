package buttondevteam.lib.player;

import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
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
import java.util.function.Supplier;

@ChromaGamerEnforcer
public abstract class ChromaGamerBase {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players/";
	private static final HashBiMap<Class<? extends ChromaGamerBase>, String> playerTypes = HashBiMap.create();
	private static final HashMap<Class<? extends ChromaGamerBase>, Supplier<? extends ChromaGamerBase>> constructors = new HashMap<>();
	private static final HashMap<Class<? extends ChromaGamerBase>, HashMap<String, ChromaGamerBase>> userCache = new HashMap<>();
	private static final ArrayList<Function<CommandSender, ? extends Optional<? extends ChromaGamerBase>>> senderConverters = new ArrayList<>();

	/**
	 * Use {@link #getConfig()} where possible; the 'id' must be always set
	 */
	protected YamlConfiguration plugindata;

	@Getter
	protected final IHaveConfig config = new IHaveConfig(this::save);

	public void init() {
		config.reset(plugindata);
	}

	/**
	 * Used for connecting with every type of user ({@link #connectWith(ChromaGamerBase)}) and to init the configs.
	 */
	public static <T extends ChromaGamerBase> void RegisterPluginUserClass(Class<T> userclass, Supplier<T> constructor) {
		if (userclass.isAnnotationPresent(UserClass.class))
			playerTypes.put(userclass, userclass.getAnnotation(UserClass.class).foldername());
		else if (userclass.isAnnotationPresent(AbstractUserClass.class))
			playerTypes.put(userclass.getAnnotation(AbstractUserClass.class).prototype(),
				userclass.getAnnotation(AbstractUserClass.class).foldername());
		else // <-- Really important
			throw new RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase");
		constructors.put(userclass, constructor);
	}

	/**
	 * Returns the folder name for the given player class.
	 *
	 * @param cl The class to get the folder from (like {@link TBMCPlayerBase} or one of it's subclasses)
	 * @return The folder name for the given type
	 * @throws RuntimeException If the class doesn't have the {@link UserClass} annotation.
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
	 * @param foldername The folder to get the class from (like "minecraft")
	 * @return The type for the given folder name or null if not found
	 */
	public static Class<? extends ChromaGamerBase> getTypeForFolder(String foldername) {
		return playerTypes.inverse().get(foldername);
	}

	/***
	 * Loads a user from disk and returns the user object. Make sure to use the subclasses' methods, where possible, like {@link TBMCPlayerBase#getPlayer(java.util.UUID, Class)}
	 *
	 * @param fname Filename without .yml, usually UUID
	 * @param cl User class
	 * @return The user object
	 */
	public static <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		HashMap<String, ? extends ChromaGamerBase> uc;
		if (userCache.containsKey(cl)) {
			uc = userCache.get(cl);
			if (uc.containsKey(fname))
				//noinspection unchecked
				return (T) uc.get(fname);
		}
		@SuppressWarnings("unchecked") T obj = (T) constructors.get(cl).get();
		final String folder = getFolderForType(cl);
		final File file = new File(TBMC_PLAYERS_DIR + folder, fname + ".yml");
		file.getParentFile().mkdirs();
		obj.plugindata = YamlConfiguration.loadConfiguration(file);
		obj.plugindata.set(folder + "_id", fname);
		obj.init();
		userCache.computeIfAbsent(cl, key -> new HashMap<>()).put(fname, obj);
		return obj;
	}

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
	 * Saves the player. It'll handle all exceptions that may happen.
	 */
	private final void save() {
		try {
			if (plugindata.getKeys(false).size() > 0)
				plugindata.save(new File(TBMC_PLAYERS_DIR + getFolder(), getFileName() + ".yml"));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while saving player to " + getFolder() + "/" + getFileName() + ".yml!", e, MainPlugin.Instance);
		}
	}

	/**
	 * Connect two accounts. Do not use for connecting two Minecraft accounts or similar. Also make sure you have the "id" tag set
	 *
	 * @param user The account to connect with
	 */
	public final <T extends ChromaGamerBase> void connectWith(T user) {
		// Set the ID, go through all linked files and connect them as well
		if (!playerTypes.containsKey(getClass()))
			throw new RuntimeException("Class not registered as a user class! Use TBMCCoreAPI.RegisterUserClass");
		final String ownFolder = getFolder();
		final String userFolder = user.getFolder();
		if (ownFolder.equalsIgnoreCase(userFolder))
			throw new RuntimeException("Do not connect two accounts of the same type! Type: " + ownFolder);
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
				ChromaGamerBase cg = getUser(otherid, entry.getKey());
				cg.plugindata.set(sourcefolder + "_id", id); // Set new IDs
				cg.config.signalChange();
				for (val item : playerTypes.entrySet()) {
					if (sourcedata.contains(item.getValue() + "_id")) {
						cg.plugindata.set(item.getValue() + "_id", sourcedata.getString(item.getValue() + "_id")); // Set all existing IDs
						cg.config.signalChange();
					}
				}
			}
		};
		sync.accept(plugindata);
		sync.accept(user.plugindata);
	}

	/**
	 * Retunrs the ID for the T typed player object connected with this one or null if no connection found.
	 *
	 * @param cl The player class to get the ID from
	 * @return The ID or null if not found
	 */
	public final <T extends ChromaGamerBase> String getConnectedID(Class<T> cl) {
		return plugindata.getString(getFolderForType(cl) + "_id");
	}

	/**
	 * Returns this player as a plugin player. This will return a new instance unless the player is online.<br>
	 * Make sure to close both the returned and this object. A try-with-resources block or two can help.<br>
	 *
	 * @param cl The target player class
	 * @return The player as a {@link T} object or null if not having an account there
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T extends ChromaGamerBase> T getAs(Class<T> cl) {
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

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, MC UUIDs, for Discord data, use Discord IDs, etc.<br>
	 * <b>Does not include .yml</b>
	 */
	public final String getFileName() {
		return plugindata.getString(getFolder() + "_id");
	}

	public final String getFolder() {
		return getFolderForType(getClass());
	}

	/**
	 * Get player information. This method calls the {@link TBMCPlayerGetInfoEvent} to get all the player information across the TBMC plugins.
	 *
	 * @param target The {@link InfoTarget} to return the info for.
	 * @return The player information.
	 */
	public final String getInfo(InfoTarget target) {
		TBMCPlayerGetInfoEvent event = new TBMCPlayerGetInfoEvent(this, target);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getResult();
	}

	public enum InfoTarget {
		MCHover, MCCommand, Discord
	}

	//-----------------------------------------------------------------

	public final ConfigData<Channel> channel = getConfig().getData("channel", Channel.GlobalChat,
		id -> Channel.getChannels().filter(ch -> ch.ID.equalsIgnoreCase((String) id)).findAny().orElse(null), ch -> ch.ID);
}
