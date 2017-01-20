package buttondevteam.lib.player;

import java.util.UUID;

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

	public static <T extends TBMCPlayerBase> T getPlayer(UUID uuid, Class<T> cl) {
		T obj = ChromaGamerBase.getUser(uuid.toString(), cl);
		obj.uuid = uuid;
		return obj;
	}
}
