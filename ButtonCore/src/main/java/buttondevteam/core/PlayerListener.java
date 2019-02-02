package buttondevteam.core;

import buttondevteam.lib.TBMCCommandPreprocessEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.player.TBMCPlayerBase;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		TBMCPlayerBase.joinPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.quitPlayer(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSystemChat(TBMCSystemChatEvent event) {
        if (event.isHandled())
	        return; // Only handle here if ButtonChat couldn't - ButtonChat doesn't even handle this
		if (Arrays.stream(event.getExceptions()).anyMatch("Minecraft"::equalsIgnoreCase))
			return;
        Bukkit.getOnlinePlayers().stream().filter(event::shouldSendTo)
	        .forEach(p -> p.sendMessage(event.getChannel().DisplayName().get().substring(0, 2) + event.getMessage()));
    }

	@EventHandler
	public void onPlayerChatPreprocess(PlayerCommandPreprocessEvent event) {
		handlePreprocess(event.getPlayer(), event.getMessage(), event);
	}

	@EventHandler
	public void onSystemChatPreprocess(ServerCommandEvent event) {
		handlePreprocess(event.getSender(), "/" + event.getCommand(), event);
		if (event.isCancelled()) event.setCommand("dontrunthiscmd"); //Bugfix
	}

	private void handlePreprocess(CommandSender sender, String message, Cancellable event) {
		if (event.isCancelled()) return;
		val ev = new TBMCCommandPreprocessEvent(sender, message);
		Bukkit.getPluginManager().callEvent(ev);
		if (ev.isCancelled())
			event.setCancelled(true); //Cancel the original event
	}

	@EventHandler
	public void onTBMCPreprocess(TBMCCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		try {
			event.setCancelled(ButtonPlugin.getCommand2MC().handleCommand(event.getSender(), event.getMessage()));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Command processing failed for sender '" + event.getSender() + "' and message '" + event.getMessage() + "'", e);
		}
	}
}