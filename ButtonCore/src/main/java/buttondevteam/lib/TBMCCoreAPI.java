package buttondevteam.lib;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.potato.DebugPotato;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
		return PluginUpdater.UpdatePlugin(name, sender, branch);
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

	private static final HashMap<String, Throwable> exceptionsToSend = new HashMap<>();
	private static final List<String> debugMessagesToSend = new ArrayList<>();

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
		synchronized (exceptionsToSend) {
			if (!event.isHandled())
				exceptionsToSend.put(sourcemsg, e);
		}
		Bukkit.getLogger().warning(sourcemsg);
		e.printStackTrace();
		if (debugPotato) {
			List<Player> devsOnline = new ArrayList<Player>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (coders.contains(player.getName())) {
					devsOnline.add(player);
				}
			}
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
		synchronized (debugMessagesToSend) {
			if (!event.isSent())
				debugMessagesToSend.add(debugMessage);
		}
	}

	/**
	 * Registers Bukkit events, handling the exceptions occurring in those events
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
		synchronized (exceptionsToSend) {
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
	}

	public static void SendUnsentDebugMessages() {
		synchronized (debugMessagesToSend) {
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
	}

	public static boolean IsTestServer() {
		return MainPlugin.Test;
	}
}