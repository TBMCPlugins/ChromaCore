package buttondevteam.core.component.updater;

import buttondevteam.lib.TBMCCoreAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginUpdater {

    private final File updatedir = Bukkit.getServer().getUpdateFolderFile();
	/**
	 * See {@link TBMCCoreAPI#UpdatePlugin(String, CommandSender, String)}
	 */
	public boolean UpdatePlugin(String name, CommandSender sender, String branch) {
        if (!updatedir.exists() && !updatedir.mkdirs()) {
            error(sender, "Failed to create update directory!");
            return false;
        }
		/*info(sender, "Checking plugin name...");
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
        if (isNotMaven(correctname, correctbranch.get())) {
			error(sender, "The plugin doesn't appear to have a pom.xml. Make sure it's a Maven project.");
			return false;
		}
		info(sender, "Updating TBMC plugin: " + correctname + " from " + correctbranch.get());
        return updatePluginJitPack(sender, correctname, correctbranch.get());*/
		info(sender, "Not implemented");
		return true;
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
}
