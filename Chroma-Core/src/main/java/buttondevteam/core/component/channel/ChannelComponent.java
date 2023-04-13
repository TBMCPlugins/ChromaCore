package buttondevteam.core.component.channel;

import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.ChromaGamerBase;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages chat channels. If disabled, only global channels will be registered.
 */
public class ChannelComponent extends Component<JavaPlugin> {
	static TBMCSystemChatEvent.BroadcastTarget roomJoinLeave;

	@Override
	protected void register(JavaPlugin plugin) {
		super.register(plugin);
		roomJoinLeave = TBMCSystemChatEvent.BroadcastTarget.add("roomJoinLeave"); //Even if it's disabled, global channels continue to work
	}

	@Override
	protected void unregister(JavaPlugin plugin) {
		super.unregister(plugin);
		TBMCSystemChatEvent.BroadcastTarget.remove(roomJoinLeave);
		roomJoinLeave = null;
	}

	@Override
	protected void enable() {
	}

	@Override
	protected void disable() {
	}

	void registerChannelCommand(Channel channel) {
		if (!ChromaUtils.isTest())
			registerCommand(new ChannelCommand(channel));
	}

	@CommandClass
	@RequiredArgsConstructor
	private static class ChannelCommand extends ICommand2MC {
		private final Channel channel;

		@Override
		public String getCommandPath() {
			return channel.identifier;
		}

		@Override
		public String[] getCommandPaths() {
			return channel.extraIdentifiers.get();
		}

		@Command2.Subcommand
		public void def(Command2MCSender senderMC, @Command2.OptionalArg @Command2.TextArg String message) {
			var sender = senderMC.getSender();
			var user = ChromaGamerBase.getFromSender(sender);
			if (user == null) {
				sender.sendMessage("§cYou can't use channels from this platform.");
				return;
			}
			if (message == null) {
				Channel oldch = user.channel.get();
				if (oldch instanceof ChatRoom)
					((ChatRoom) oldch).leaveRoom(sender);
				if (oldch.equals(channel))
					user.channel.set(Channel.GlobalChat);
				else {
					user.channel.set(channel);
					if (channel instanceof ChatRoom)
						((ChatRoom) channel).joinRoom(sender);
				}
				sender.sendMessage("§6You are now talking in: §b" + user.channel.get().displayName.get());
			} else
				TBMCChatAPI.SendChatMessage(ChatMessage.builder(sender, user, message).fromCommand(true)
					.permCheck(senderMC.getPermCheck()).build(), channel);
		}
	}
}
