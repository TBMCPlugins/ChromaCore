package buttondevteam.lib.player;

import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ChromaGamerEnforcer
public abstract class ChromaGamerBase {
	private static final String TBMC_PLAYERS_DIR = "TBMC/players/";
	private static final ArrayList<Function<CommandSender, ? extends Optional<? extends ChromaGamerBase>>> senderConverters = new ArrayList<>();
	/**
	 * Holds data per user class
	 */
	private static final HashMap<Class<? extends ChromaGamerBase>, StaticUserData<?>> staticDataMap = new HashMap<>();

	/**
	 * Use {@link #getConfig()} where possible; the 'id' must be always set
	 */
	//protected YamlConfiguration plugindata;

	@Getter
	protected final IHaveConfig config = new IHaveConfig(this::save);
	protected CommonUserData<?> commonUserData;

	/**
	 * Used for connecting with every type of user ({@link #connectWith(ChromaGamerBase)}) and to init the configs.
	 * Also, to construct an instance if an abstract class is provided.
	 */
	public static <T extends ChromaGamerBase> void RegisterPluginUserClass(Class<T> userclass, Supplier<T> constructor) {
		Class<? extends T> cl;
		String folderName;
		if (userclass.isAnnotationPresent(UserClass.class)) {
			cl = userclass;
			folderName = userclass.getAnnotation(UserClass.class).foldername();
		} else if (userclass.isAnnotationPresent(AbstractUserClass.class)) {
			var ucl = userclass.getAnnotation(AbstractUserClass.class).prototype();
			if (!userclass.isAssignableFrom(ucl))
				throw new RuntimeException("The prototype class (" + ucl.getSimpleName() + ") must be a subclass of the userclass parameter (" + userclass.getSimpleName() + ")!");
			//noinspection unchecked
			cl = (Class<? extends T>) ucl;
			folderName = userclass.getAnnotation(AbstractUserClass.class).foldername();
		} else // <-- Really important
			throw new RuntimeException("Class not registered as a user class! Use @UserClass or TBMCPlayerBase");
		var sud = new StaticUserData<T>(folderName);
		sud.getConstructors().put(cl, constructor);
		sud.getConstructors().put(userclass, constructor); // Alawys register abstract and prototype class (TBMCPlayerBase and TBMCPlayer)
		staticDataMap.put(userclass, sud);
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
		synchronized (staticDataMap) {
			return staticDataMap.entrySet().stream().filter(e -> e.getValue().getFolder().equalsIgnoreCase(foldername))
				.map(Map.Entry::getKey).findAny().orElse(null);
		}
	}

	/***
	 * Retrieves a user from cache or loads it from disk.
	 *
	 * @param fname Filename without .yml, the user's identifier for that type
	 * @param cl User class
	 * @return The user object
	 */
	public static synchronized <T extends ChromaGamerBase> T getUser(String fname, Class<T> cl) {
		StaticUserData<?> staticUserData = null;
		for (var sud : staticDataMap.entrySet()) {
			if (sud.getKey().isAssignableFrom(cl)) {
				staticUserData = sud.getValue();
				break;
			}
		}
		if (staticUserData == null)
			throw new RuntimeException("User class not registered! Use @UserClass or @AbstractUserClass");
		var commonUserData = staticUserData.getUserDataMap().get(fname);
		if (commonUserData == null) {
			final String folder = staticUserData.getFolder();
			final File file = new File(TBMC_PLAYERS_DIR + folder, fname + ".yml");
			file.getParentFile().mkdirs();
			var playerData = YamlConfiguration.loadConfiguration(file);
			commonUserData = new CommonUserData<>(playerData);
			playerData.set(staticUserData.getFolder() + "_id", fname);
			staticUserData.getUserDataMap().put(fname, commonUserData);
		}
		if (commonUserData.getUserCache().containsKey(cl))
			return (T) commonUserData.getUserCache().get(cl);
		T obj;
		if (staticUserData.getConstructors().containsKey(cl))
			//noinspection unchecked
			obj = (T) staticUserData.getConstructors().get(cl).get();
		else {
			try {
				obj = cl.getConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to create new instance of user of type " + cl.getSimpleName() + "!", e);
			}
		}
		obj.commonUserData = commonUserData;
		obj.init();
		obj.scheduleUncache();
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
	public static ChromaGamerBase getFromSender(CommandSender sender) { // TODO: Use Command2Sender
		for (val converter : senderConverters) {
			val ocg = converter.apply(sender);
			if (ocg.isPresent())
				return ocg.get();
		}
		return null;
	}

	public static void saveUsers() {
		synchronized (staticDataMap) {
			for (var sud : staticDataMap.values())
				for (var cud : sud.getUserDataMap().values())
					ConfigData.saveNow(cud.getPlayerData()); //Calls save()
		}
	}

	protected void init() {
		config.reset(commonUserData.getPlayerData());
	}

	/**
	 * Saves the player. It'll handle all exceptions that may happen. Called automatically.
	 */
	protected void save() {
		try {
			if (commonUserData.getPlayerData().getKeys(false).size() > 0)
				commonUserData.getPlayerData().save(new File(TBMC_PLAYERS_DIR + getFolder(), getFileName() + ".yml"));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while saving player to " + getFolder() + "/" + getFileName() + ".yml!", e, MainPlugin.Instance);
		}
	}

	/**
	 * Removes the user from the cache. This will be called automatically after some time by default.
	 */
	public void uncache() {
		final var userCache = commonUserData.getUserCache();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (userCache) {
			if (userCache.containsKey(getClass()))
				if (userCache.remove(getClass()) != this)
					throw new IllegalStateException("A different player instance was cached!");
		}
	}

	protected void scheduleUncache() {
		Bukkit.getScheduler().runTaskLaterAsynchronously(MainPlugin.Instance, this::uncache, 2 * 60 * 60 * 20); //2 hours
	}

	/**
	 * Connect two accounts. Do not use for connecting two Minecraft accounts or similar. Also make sure you have the "id" tag set.
	 *
	 * @param user The account to connect with
	 */
	public final <T extends ChromaGamerBase> void connectWith(T user) {
		// Set the ID, go through all linked files and connect them as well
		final String ownFolder = getFolder();
		final String userFolder = user.getFolder();
		if (ownFolder.equalsIgnoreCase(userFolder))
			throw new RuntimeException("Do not connect two accounts of the same type! Type: " + ownFolder);
		var ownData = commonUserData.getPlayerData();
		var userData = user.commonUserData.getPlayerData();
		userData.set(ownFolder + "_id", ownData.getString(ownFolder + "_id"));
		ownData.set(userFolder + "_id", userData.getString(userFolder + "_id"));
		config.signalChange();
		user.config.signalChange();
		Consumer<YamlConfiguration> sync = sourcedata -> {
			final String sourcefolder = sourcedata == ownData ? ownFolder : userFolder;
			final String id = sourcedata.getString(sourcefolder + "_id");
			for (val entry : staticDataMap.entrySet()) { // Set our ID in all files we can find, both from our connections and the new ones
				if (entry.getKey() == getClass() || entry.getKey() == user.getClass())
					continue;
				var entryFolder = entry.getValue().getFolder();
				final String otherid = sourcedata.getString(entryFolder + "_id");
				if (otherid == null)
					continue;
				ChromaGamerBase cg = getUser(otherid, entry.getKey());
				var cgData = cg.commonUserData.getPlayerData();
				cgData.set(sourcefolder + "_id", id); // Set new IDs
				for (val item : staticDataMap.entrySet()) {
					var itemFolder = item.getValue().getFolder();
					if (sourcedata.contains(itemFolder + "_id")) {
						cgData.set(itemFolder + "_id", sourcedata.getString(itemFolder + "_id")); // Set all existing IDs
					}
				}
				cg.config.signalChange();
			}
		};
		sync.accept(ownData);
		sync.accept(userData);
	}

	/**
	 * Returns the ID for the T typed player object connected with this one or null if no connection found.
	 *
	 * @param cl The player class to get the ID from
	 * @return The ID or null if not found
	 */
	public final <T extends ChromaGamerBase> String getConnectedID(Class<T> cl) {
		return commonUserData.getPlayerData().getString(getFolderForType(cl) + "_id");
	}

	/**
	 * Returns a player instance of the given type that represents the same player. This will return a new instance unless the player is cached.<br>
	 * If the class is a subclass of the current class then the same ID is used, otherwise, a connected ID is used, if found.
	 *
	 * @param cl The target player class
	 * @return The player as a {@link T} object or null if the user doesn't have an account there
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
		var playerData = commonUserData.getPlayerData();
		if (!playerData.contains(newfolder + "_id"))
			return null;
		return getUser(playerData.getString(newfolder + "_id"), cl);
	}

	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, MC UUIDs, for Discord data, Discord IDs, etc.<br>
	 * <b>Does not include .yml</b>
	 */
	public final String getFileName() {
		return commonUserData.getPlayerData().getString(getFolder() + "_id");
	}

	/**
	 * This method returns the folder that this player data is stored in. For example: "minecraft".
	 */
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

	public final ConfigData<Channel> channel = config.getData("channel", Channel.GlobalChat,
		id -> Channel.getChannels().filter(ch -> ch.ID.equalsIgnoreCase((String) id)).findAny().orElse(null), ch -> ch.ID);
}
