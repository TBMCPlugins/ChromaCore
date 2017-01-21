package buttondevteam.lib.player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TBMCPlayerBase extends ChromaGamerBase {
	private static final String FOLDER_NAME = "minecraft";
	protected UUID uuid;

	public UUID getUUID() {
		return uuid;
	}

	public abstract String getPluginName();

	@Override
	public String getFileName() {
		return getUUID().toString();
	}

	// protected ConfigurationSection plugindata;

	/*
	 * public static void load() { super.load(); plugindata = super.plugindata.getConfigurationSection(getPluginName()); if (plugindata == null) plugindata =
	 * super.plugindata.createSection(getPluginName()); } protected void save() { plugindata = super.plugindata.createSection(getPluginName(), plugindata.getValues(true)); super.save(); }
	 */

	static {
		addPlayerType(TBMCPlayerBase.class, FOLDER_NAME);
	}

	@SuppressWarnings("unchecked")
	public static <T extends TBMCPlayerBase> T getPlayer(UUID uuid, Class<T> cl) {
		if (playermap.containsKey(uuid + "-" + cl.getSimpleName()))
			return (T) playermap.get(uuid + "-" + cl.getSimpleName());
		T obj = ChromaGamerBase.getUser(uuid.toString(), cl);
		obj.uuid = uuid;
		return obj;
	}

	/**
	 * Key: UUID-Class
	 */
	static final ConcurrentHashMap<String, TBMCPlayerBase> playermap = new ConcurrentHashMap<>();

	/**
	 * Gets the TBMCPlayer object as a specific plugin player, keeping it's data *
	 * 
	 * @param p
	 *            Player to get
	 * @param cl
	 *            The TBMCPlayer subclass
	 */
	public <T extends TBMCPlayerBase> T asPluginPlayer(Class<T> cl) {
		return getPlayer(uuid, cl);
	}
}
