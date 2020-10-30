package buttondevteam.core;

import buttondevteam.core.component.towny.TownyComponent;
import buttondevteam.lib.*;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.Command2MCSender;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		var p = event.getPlayer();
		TBMCPlayer player = TBMCPlayerBase.getPlayer(p.getUniqueId(), TBMCPlayer.class);
		String pname = player.PlayerName.get();
		if (pname.length() == 0) {
			player.PlayerName.set(p.getName());
			MainPlugin.Instance.getLogger().info("Player name saved: " + player.PlayerName.get());
		} else if (!p.getName().equals(pname)) {
			TownyComponent.renameInTowny(pname, p.getName());
			MainPlugin.Instance.getLogger().info(pname + " renamed to " + p.getName());
			player.PlayerName.set(p.getName());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void OnPlayerLeave(PlayerQuitEvent event) {
		TBMCPlayerBase.getPlayer(event.getPlayer().getUniqueId(), TBMCPlayer.class).uncache();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSystemChat(TBMCSystemChatEvent event) {
		if (event.isHandled())
			return; // Only handle here if ButtonChat couldn't - ButtonChat doesn't even handle this
		if (Arrays.stream(event.getExceptions()).anyMatch("Minecraft"::equalsIgnoreCase))
			return;
		Bukkit.getOnlinePlayers().stream().filter(event::shouldSendTo)
			.forEach(p -> p.sendMessage(event.getChannel().DisplayName.get().substring(0, 2) + event.getMessage()));
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
		val cg = ChromaGamerBase.getFromSender(sender);
		if (cg == null) throw new RuntimeException("Couldn't get user from sender for " + sender.getName() + "!");
		val ev = new TBMCCommandPreprocessEvent(sender, cg.channel.get(), message, sender);
		Bukkit.getPluginManager().callEvent(ev);
		if (ev.isCancelled())
			event.setCancelled(true); //Cancel the original event
	}

	@EventHandler
	public void onTBMCPreprocess(TBMCCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		try {
			event.setCancelled(ButtonPlugin.getCommand2MC().handleCommand(new Command2MCSender(event.getSender(), event.getChannel(), event.getPermCheck()), event.getMessage()));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Command processing failed for sender '" + event.getSender() + "' and message '" + event.getMessage() + "'", e, MainPlugin.Instance);
		}
	}

	@EventHandler(priority = EventPriority.HIGH) //The one in the chat plugin is set to highest
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return; //The chat plugin should cancel it after this handler
		val cp = TBMCPlayer.getPlayer(event.getPlayer().getUniqueId(), TBMCPlayer.class);
		TBMCChatAPI.SendChatMessage(ChatMessage.builder(event.getPlayer(), cp, event.getMessage()).build());
		//Not cancelling the original event here, it's cancelled in the chat plugin
		//This way other plugins can deal with the MC formatting if the chat plugin isn't present, but other platforms still get the message
	}

	@EventHandler(priority = EventPriority.HIGH) //The one in the chat plugin is set to highest
	public void onPlayerChat(TBMCChatEvent event) {
		if (event.isCancelled())
			return;
		if (!MainPlugin.Instance.isChatHandlerEnabled()) return;
		if (event.getOrigin().equals("Minecraft")) return; //Let other plugins handle MC messages
		var channel = event.getChannel();
		String msg = MainPlugin.Instance.chatFormat.get()
			.replace("{channel}", channel.DisplayName.get())
			.replace("{origin}", event.getOrigin().substring(0, 1))
			.replace("{name}", ChromaUtils.getDisplayName(event.getSender()))
			.replace("{message}", String.format("§%x%s", channel.Color.get().ordinal(), event.getMessage()));
		for (Player player : Bukkit.getOnlinePlayers())
			if (event.shouldSendTo(player))
				player.sendMessage(msg);
		Bukkit.getConsoleSender().sendMessage(msg);
	}
}