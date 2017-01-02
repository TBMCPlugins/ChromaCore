package buttondevteam.lib;

import org.bukkit.configuration.ConfigurationSection;

public abstract class ChromaGamerBase {
	/**
	 * This method returns the filename for this player data. For example, for Minecraft-related data, use MC UUIDs, for Discord data, use Discord IDs, etc.
	 */
	public abstract String getFileName();

	/**
	 * This method returns the folder the file is in. For example, for Minecraft data, this should be "minecraft", for Discord, "discord", etc.
	 */
	public abstract String getFolder();

	protected ConfigurationSection plugindata;
}
