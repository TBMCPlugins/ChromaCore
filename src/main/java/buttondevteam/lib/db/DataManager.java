package buttondevteam.lib.db;

import java.util.UUID;

import javax.persistence.PersistenceException;

import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;

import buttondevteam.core.MainPlugin;

public final class DataManager {
	private static EbeanServer database;

	/**
	 * Can be only used once and is used by {@link MainPlugin}
	 * 
	 * @param database
	 *            The database to set
	 */
	public static void setDatabase(EbeanServer database) {
		DataManager.database = database;
	}

	public static <T extends CData> T load(Class<T> cl, UUID id) {
		return database.find(cl, id);
	}

	public static <T extends CData> void save(T obj) {
		database.save(obj);
	}

}
