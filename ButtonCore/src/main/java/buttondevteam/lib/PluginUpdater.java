package buttondevteam.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginUpdater {
	private PluginUpdater() {
	}

    public static final File updatedir = new File("TBMC", "pluginupdates");
	/**
	 * See {@link TBMCCoreAPI#UpdatePlugin(String, CommandSender, String)}
	 */
	public static boolean UpdatePlugin(String name, CommandSender sender, String branch) {
        if (!updatedir.exists() && !updatedir.mkdirs()) {
            error(sender, "Failed to create update directory!");
            return false;
        }
		info(sender, "Checking plugin name...");
		List<String> plugins = GetPluginNames();
		String correctname = null;
		for (String plugin : plugins) {
			if (plugin.equalsIgnoreCase(name)) {
				correctname = plugin; // Fixes capitalization
				break;
			}
		}
		if (correctname == null) {
			error(sender, "Can't find plugin: " + name);
			return false;
		}
		info(sender, "Checking branch name...");
		if (!TBMCCoreAPI.IsTestServer() && !branch.equalsIgnoreCase("master")) {
			error(sender, "The server is in production mode, updating only allowed from master!");
			return false;
		}
		Optional<String> correctbranch = GetPluginBranches(correctname).stream().filter(b -> b.equalsIgnoreCase(branch))
				.findAny();
		if (!correctbranch.isPresent()) {
			error(sender, "Can't find branch \"" + branch + "\" for plugin \"" + correctname + "\"");
			return false;
		}
		if (!isMaven(correctname, correctbranch.get())) {
			error(sender, "The plugin doesn't appear to have a pom.xml. Make sure it's a Maven project.");
			return false;
		}
		info(sender, "Updating TBMC plugin: " + correctname + " from " + correctbranch.get());
        return updatePluginJitPack(sender, correctname, correctbranch.get());
	}

	private static boolean updatePluginJitPack(CommandSender sender, String correctname,
                                               String correctbranch) {
		URL url;
        File result = new File(updatedir, correctname + ".jar");
		try {
			url = new URL("https://jitpack.io/com/github/TBMCPlugins/"
					+ (correctname.equals("ButtonCore") ? "ButtonCore/ButtonCore" : correctname) + "/"
                    + correctbranch + "-SNAPSHOT/" + correctname + "-" + correctbranch + "-SNAPSHOT.jar"); // ButtonCore exception required since it hosts Towny as well
			FileUtils.copyURLToFile(url, result);
			if (!result.exists() || result.length() < 25) {
				result.delete();
                error(sender, "The downloaded JAR for " + correctname + " from " + correctbranch
						+ " is too small (smnaller than 25 bytes). Am I downloading from the right place?");
				return false;
			} else {
                info(sender, "Updating plugin " + correctname + " from " + correctbranch + " done!");
				return true;
			}
		} catch (FileNotFoundException e) {
			error(sender,
                    "Can't find JAR for " + correctname + " from " + correctbranch
							+ ", the build probably failed. Build log (scroll to bottom):" + "\n"
                            + "https://jitpack.io/com/github/TBMCPlugins/" + correctname + "/" + correctbranch
							+ "-SNAPSHOT/build.log\nIf you'd like to rebuild the same commit, go to:\nhttps://jitpack.io/#TBMCPlugins/"
							+ correctname + "\nAnd delete the newest build.");
		} catch (IOException e) {
			error(sender, "IO error while updating " + correctname + "\n" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			error(sender, "Unknown error while updating " + correctname + ": " + e);
		}
		return false;
	}

	/**
	 * Checks if pom.xml is present for the project.
	 * 
	 * @param pluginname
	 *            Does not have to match case
	 * @param branch
	 *            Does not have to match case
	 */
	public static boolean isMaven(String pluginname, String branch) {
		try {
			return !TBMCCoreAPI
					.DownloadString(
							"https://raw.githubusercontent.com/TBMCPlugins/" + pluginname + "/" + branch + "/pom.xml")
					.equals("404: Not Found\n");
		} catch (IOException e1) {
			return false;
		}
	}

	private static void error(CommandSender sender, String message) {
		if (!sender.equals(Bukkit.getConsoleSender()))
			Bukkit.getLogger().warning(message);
		sender.sendMessage("§c" + message);
	}

	private static void info(CommandSender sender, String message) {
		if (!sender.equals(Bukkit.getConsoleSender()))
			Bukkit.getLogger().info(message);
		sender.sendMessage("§b" + message);
	}

	/**
	 * Retrieves all the repository names from the GitHub organization.
	 * 
	 * @return A list of names
	 */
	public static List<String> GetPluginNames() {
		List<String> ret = new ArrayList<>();
		try {
			String resp = TBMCCoreAPI.DownloadString("https://api.github.com/orgs/TBMCPlugins/repos");
			JsonArray arr = new JsonParser().parse(resp).getAsJsonArray();
			for (JsonElement obj : arr) {
				JsonObject jobj = obj.getAsJsonObject();
				ret.add(jobj.get("name").getAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Retrieves all the branches from the plugin repository.
	 * 
	 * @return A list of names
	 */
	public static List<String> GetPluginBranches(String plugin) {
		List<String> ret = new ArrayList<>();
		try {
			String resp = TBMCCoreAPI
					.DownloadString("https://api.github.com/repos/TBMCPlugins/" + plugin + "/branches");
			JsonArray arr = new JsonParser().parse(resp).getAsJsonArray();
			for (JsonElement obj : arr) {
				JsonObject jobj = obj.getAsJsonObject();
				ret.add(jobj.get("name").getAsString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static class UpdatedEvent extends Event {
		private static final HandlerList handlers = new HandlerList();

		private JsonObject data;

		public UpdatedEvent(JsonObject data) {
			this.data = data;
		}

		public JsonObject getData() {
			return data;
		}

		@Override
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}
	}
}
