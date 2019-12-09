package buttondevteam.core.component.spawn;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import com.earth2me.essentials.Trade;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.math.BigDecimal;

/**
 * Provides a /spawn command that works with BungeeCord. Make sure to set up on each server.
 */
public class SpawnComponent extends Component<MainPlugin> implements PluginMessageListener {
	@Override
	protected void enable() {
		registerCommand(new SpawnCommand());
		if (targetServer().get().length() == 0) {
			spawnloc = MultiverseCore.getPlugin(MultiverseCore.class).getMVWorldManager().getFirstSpawnWorld()
				.getSpawnLocation();
		}

		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(getPlugin(), "BungeeCord");
		Bukkit.getServer().getMessenger().registerIncomingPluginChannel(getPlugin(), "BungeeCord", this);
	}

	@Override
	protected void disable() {
		Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(getPlugin(), "BungeeCord");
		Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(getPlugin(), "BungeeCord");
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
		if (targetServer().get().length() != 0)
			return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if ("ChromaCore-Spawn".equals(subchannel)) {
			// Use the code sample in the 'Response' sections below to read
			// the data.
			System.out.println("Heh nice");
			short len = in.readShort();
			byte[] msgbytes = new byte[len];
			in.readFully(msgbytes);

			try {
				DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
				String somedata = msgin.readUTF(); // Read the data in the same way you wrote it
				if (!"SendToSpawn".equals(somedata)) {
					System.out.println("somedata: " + somedata);
					return;
				}
				player.teleport(spawnloc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			System.out.println("Subchannel: " + subchannel);
	}

	/**
	 * The BungeeCord server that has the spawn. Set to empty if this server is the target.
	 */
	private ConfigData<String> targetServer() {
		return getConfig().getData("targetServer", "");
	}

	private Location spawnloc;

	@CommandClass(helpText = {
		"Spawn",
		"Teleport to spawn."
	})
	public class SpawnCommand extends ICommand2MC {
		@SuppressWarnings("UnstableApiUsage")
		@Command2.Subcommand
		public void def(Player player) {
			if (targetServer().get().length() == 0) {
				player.sendMessage("§bTeleporting to spawn...");
				try {
					if (MainPlugin.ess != null)
						MainPlugin.ess.getUser(player).getTeleport()
							.teleport(spawnloc, new Trade(BigDecimal.ZERO, MainPlugin.ess), PlayerTeleportEvent.TeleportCause.COMMAND);
					else
						player.teleport(spawnloc);
				} catch (Exception e) {
					player.sendMessage("§cFailed to teleport: " + e);
				}
				return;
			}
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(targetServer().get());

			player.sendPluginMessage(getPlugin(), "BungeeCord", out.toByteArray());

			Bukkit.getScheduler().runTask(getPlugin(), () -> { //Delay it a bit
				ByteArrayDataOutput outt = ByteStreams.newDataOutput();
				outt.writeUTF("ForwardToPlayer"); // So BungeeCord knows to forward it
				outt.writeUTF(player.getName());
				outt.writeUTF("ChromaCore-Spawn"); // The channel name to check if this your data

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try {
					msgout.writeUTF("SendToSpawn"); // You can do anything you want with msgout
				} catch (IOException exception) {
					exception.printStackTrace();
				}

				outt.writeShort(msgbytes.toByteArray().length);
				outt.write(msgbytes.toByteArray());

				player.sendPluginMessage(getPlugin(), "BungeeCord", outt.toByteArray());
			});
		}
	}
}
