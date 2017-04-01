package buttondevteam.lib;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.google.gson.*;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.potato.DebugPotato;

public class TBMCCoreAPI {
	static List<String> coders = new ArrayList<String>() {
		private static final long serialVersionUID = -4462159250738367334L;
		{
			add("Alisolarflare");
			add("NorbiPeti");
			add("iie");
			add("thewindmillman");
			add("mayskam1995");
		}
	};

	/**
	 * Updates or installs the specified plugin. The plugin must use Maven.
	 * 
	 * @param name
	 *            The plugin's repository name.
	 * @param sender
	 *            The command sender (if not console, messages will be printed to console as well).
	 */
	public static void UpdatePlugin(String name, CommandSender sender) {
		UpdatePlugin(name, sender, "master");
	}

	/**
	 * Updates or installs the specified plugin from the specified branch. The plugin must use Maven.
	 * 
	 * @param name
	 *            The plugin's repository name.
	 * @param sender
	 *            The command sender (if not console, messages will be printed to console as well).
	 * @param branch
	 *            The branch to download the plugin from.
	 * @return Success or not
	 */
	public static boolean UpdatePlugin(String name, CommandSender sender, String branch) {
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
		info(sender, "Updating TBMC plugin: " + correctname + " from " + correctbranch.get());
		URL url;
		final boolean isWindows = System.getProperty("os.name").contains("Windows");
		File result = new File("plugins/" + correctname + (isWindows ? ".jar" : ".jar_tmp"));
		File finalresult = new File("plugins/" + correctname + ".jar");
		try {
			url = new URL("https://jitpack.io/com/github/TBMCPlugins/"
					+ (correctname.equals("ButtonCore") ? "ButtonCore/ButtonCore" : correctname) + "/"
					+ correctbranch.get() + "-SNAPSHOT/" + correctname + "-" + correctbranch.get() + "-SNAPSHOT.jar"); // ButtonCore exception required since it hosts Towny as well
			FileUtils.copyURLToFile(url, result);
			if (!result.exists() || result.length() < 25) {
				result.delete();
				error(sender, "The downloaded JAR for " + correctname + " from " + correctbranch.get()
						+ " is too small (smnaller than 25 bytes). Am I downloading from the right place?");
				return false;
			} else {
				if (!isWindows)
					Files.move(result.toPath(), finalresult.toPath(), StandardCopyOption.REPLACE_EXISTING);
				info(sender, "Updating plugin " + correctname + " from " + correctbranch.get() + " done!");
				return true;
			}
		} catch (FileNotFoundException e) {
			error(sender,
					"Can't find JAR for " + correctname + " from " + correctbranch.get()
							+ ", the build probably failed. Build log (scroll to bottom):" + "\n"
							+ "https://jitpack.io/com/github/TBMCPlugins/" + correctname + "/" + correctbranch.get()
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
			String resp = DownloadString("https://api.github.com/orgs/TBMCPlugins/repos");
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
			String resp = DownloadString("https://api.github.com/repos/TBMCPlugins/" + plugin + "/branches");
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

	public static String DownloadString(String urlstr) throws MalformedURLException, IOException {
		URL url = new URL(urlstr);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TBMCPlugins");
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		in.close();
		return body;
	}

	private static HashMap<String, Throwable> exceptionsToSend = new HashMap<>();
	private static List<String> debugMessagesToSend = new ArrayList<>();

	/**
	 * Send exception to the {@link TBMCExceptionEvent}.
	 * 
	 * @param sourcemsg
	 *            A message that is shown at the top of the exception (before the exception's message)
	 * @param e
	 *            The exception to send
	 */
	public static void SendException(String sourcemsg, Throwable e) {
		SendException(sourcemsg, e, false);
	}

	public static void SendException(String sourcemsg, Throwable e, boolean debugPotato) {
		SendUnsentExceptions();
		TBMCExceptionEvent event = new TBMCExceptionEvent(sourcemsg, e);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isHandled())
			exceptionsToSend.put(sourcemsg, e);
		Bukkit.getLogger().warning(sourcemsg);
		e.printStackTrace();
		if (debugPotato) {
			List<Player> devsOnline = new ArrayList<Player>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (coders.contains(player.getName())) {
					devsOnline.add(player);
				}
			}
			;
			if (!devsOnline.isEmpty()) {
				DebugPotato potato = new DebugPotato()
						.setMessage(new String[] { //
								"§b§o" + e.getClass().getSimpleName(), //
								"§c§o" + sourcemsg, //
								"§a§oFind a dev to fix this issue" })
						.setType(e instanceof IOException ? "Throwable Potato"
								: e instanceof ClassCastException ? "Squished Potato"
										: e instanceof NullPointerException ? "Plain Potato"
												: e instanceof StackOverflowError ? "Chips" : "Error Potato");
				for (Player dev : devsOnline) {
					potato.Send(dev);
				}
			}
		}
	}

	public static void sendDebugMessage(String debugMessage) {
		SendUnsentDebugMessages();
		TBMCDebugMessageEvent event = new TBMCDebugMessageEvent(debugMessage);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isSent())
			debugMessagesToSend.add(debugMessage);
	}

	/**
	 * Registers Bukkit events, handling the exceptions occuring in those events
	 * 
	 * @param listener
	 *            The class that handles the events
	 * @param plugin
	 *            The plugin which the listener belongs to
	 */
	public static void RegisterEventsForExceptions(Listener listener, Plugin plugin) {
		EventExceptionHandler.registerEvents(listener, plugin, new EventExceptionCoreHandler());
	}

	public static <T extends ChromaGamerBase> void RegisterUserClass(Class<T> userclass) {
		ChromaGamerBase.RegisterPluginUserClass(userclass);
	}

	/**
	 * Send exceptions that haven't been sent (their events didn't get handled). This method is used by the DiscordPlugin's ready event
	 */
	public static void SendUnsentExceptions() {
		if (exceptionsToSend.size() > 20) {
			exceptionsToSend.clear(); // Don't call more and more events if all the handler plugins are unloaded
			Bukkit.getLogger().warning("Unhandled exception list is over 20! Clearing!");
		}
		for (Entry<String, Throwable> entry : exceptionsToSend.entrySet()) {
			TBMCExceptionEvent event = new TBMCExceptionEvent(entry.getKey(), entry.getValue());
			Bukkit.getPluginManager().callEvent(event);
			if (event.isHandled())
				exceptionsToSend.remove(entry.getKey());
		}
	}

	public static void SendUnsentDebugMessages() {
		if (debugMessagesToSend.size() > 20) {
			debugMessagesToSend.clear(); // Don't call more and more DebugMessages if all the handler plugins are unloaded
			Bukkit.getLogger().warning("Unhandled Debug Message list is over 20! Clearing!");
		}
		for (String message : debugMessagesToSend) {
			TBMCDebugMessageEvent event = new TBMCDebugMessageEvent(message);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isSent())
				debugMessagesToSend.remove(message);
		}
	}

	public static boolean IsTestServer() {
		return MainPlugin.Test;
	}
}