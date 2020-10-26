package buttondevteam.lib;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.potato.DebugPotato;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TBMCCoreAPI {
	static final List<String> coders = new ArrayList<String>() {
		private static final long serialVersionUID = -4462159250738367334L;

		{
			add("Alisolarflare");
			add("NorbiPeti");
			add("iie");
			add("thewindmillman");
			add("mayskam1995");
		}
	};

	public static String DownloadString(String urlstr) throws IOException {
		URL url = new URL(urlstr);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TBMCPlugins");
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		Scanner s = new Scanner(in).useDelimiter("\\A");
		String body = s.hasNext() ? s.next() : "";
		in.close();
		return body;
	}

	private static final HashMap<String, Throwable> exceptionsToSend = new HashMap<>();
	private static final List<String> debugMessagesToSend = new ArrayList<>();

	/**
	 * Send exception to the {@link TBMCExceptionEvent}.
	 *
	 * @param sourcemsg A message that is shown at the top of the exception (before the exception's message)
	 * @param e         The exception to send
	 */
	public static void SendException(String sourcemsg, Throwable e, Component<?> component) {
		SendException(sourcemsg, e, false, component::logWarn);
	}

	/**
	 * Send exception to the {@link TBMCExceptionEvent}.
	 *
	 * @param sourcemsg A message that is shown at the top of the exception (before the exception's message)
	 * @param e         The exception to send
	 */
	public static void SendException(String sourcemsg, Throwable e, JavaPlugin plugin) {
		SendException(sourcemsg, e, false, plugin.getLogger()::warning);
	}

	public static void SendException(String sourcemsg, Throwable e, boolean debugPotato, Consumer<String> logWarn) {
		try {
			SendUnsentExceptions();
			TBMCExceptionEvent event = new TBMCExceptionEvent(sourcemsg, e);
			Bukkit.getPluginManager().callEvent(event);
			synchronized (exceptionsToSend) {
				if (!event.isHandled())
					exceptionsToSend.put(sourcemsg, e);
			}
			logWarn.accept(sourcemsg);
			e.printStackTrace();
			if (debugPotato) {
				List<Player> devsOnline = new ArrayList<>();
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (coders.contains(player.getName())) {
						devsOnline.add(player);
					}
				}
				if (!devsOnline.isEmpty()) {
					DebugPotato potato = new DebugPotato()
						.setMessage(new String[]{ //
							"§b§o" + e.getClass().getSimpleName(), //
							"§c§o" + sourcemsg, //
							"§a§oFind a dev to fix this issue"})
						.setType(e instanceof IOException ? "Throwable Potato"
							: e instanceof ClassCastException ? "Squished Potato"
							: e instanceof NullPointerException ? "Plain Potato"
							: e instanceof StackOverflowError ? "Chips" : "Error Potato");
					for (Player dev : devsOnline) {
						potato.Send(dev);
					}
				}
			}
		} catch (Exception ee) {
			System.err.println("Failed to send exception!");
			ee.printStackTrace();
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

	private static EventExceptionCoreHandler eventExceptionCoreHandler;

	/**
	 * Registers Bukkit events, handling the exceptions occurring in those events
	 *
	 * @param listener The class that handles the events
	 * @param plugin   The plugin which the listener belongs to
	 */
	public static void RegisterEventsForExceptions(Listener listener, Plugin plugin) {
		if (eventExceptionCoreHandler == null) eventExceptionCoreHandler = new EventExceptionCoreHandler();
		EventExceptionHandler.registerEvents(listener, plugin, eventExceptionCoreHandler);
	}

	public static <T extends ChromaGamerBase> void RegisterUserClass(Class<T> userclass, Supplier<T> constructor) {
		ChromaGamerBase.RegisterPluginUserClass(userclass, constructor);
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
			for (Iterator<Entry<String, Throwable>> iterator = exceptionsToSend.entrySet().iterator(); iterator.hasNext(); ) {
				Entry<String, Throwable> entry = iterator.next();
				TBMCExceptionEvent event = new TBMCExceptionEvent(entry.getKey(), entry.getValue());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isHandled())
					iterator.remove();
			}
		}
	}

	public static void SendUnsentDebugMessages() {
		synchronized (debugMessagesToSend) {
			if (debugMessagesToSend.size() > 20) {
				debugMessagesToSend.clear(); // Don't call more and more DebugMessages if all the handler plugins are unloaded
				Bukkit.getLogger().warning("Unhandled Debug Message list is over 20! Clearing!");
			}
			for (Iterator<String> iterator = debugMessagesToSend.iterator(); iterator.hasNext(); ) {
				String message = iterator.next();
				TBMCDebugMessageEvent event = new TBMCDebugMessageEvent(message);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isSent())
					iterator.remove();
			}
		}
	}

	public static boolean IsTestServer() {
		if (MainPlugin.Instance == null) return true;
		return MainPlugin.Instance.test.get();
	}
}