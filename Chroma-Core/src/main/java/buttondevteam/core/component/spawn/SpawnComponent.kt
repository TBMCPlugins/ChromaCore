package buttondevteam.core.component.spawn

import buttondevteam.core.MainPlugin
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import com.earth2me.essentials.Trade
import com.google.common.io.ByteStreams
import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.*
import java.math.BigDecimal

/**
 * Provides a /spawn command that works with BungeeCord. Make sure to set up on each server.
 * Requires Multiverse-Core.
 */
@ComponentMetadata(enabledByDefault = false)
class SpawnComponent : Component<MainPlugin>(), PluginMessageListener {
    override fun enable() {
        registerCommand(SpawnCommand())
        if (targetServer.get().isEmpty()) {
            spawnloc = MultiverseCore.getPlugin(MultiverseCore::class.java).mvWorldManager.firstSpawnWorld.spawnLocation
        }
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(plugin, "BungeeCord")
        Bukkit.getServer().messenger.registerIncomingPluginChannel(plugin, "BungeeCord", this)
    }

    override fun disable() {
        Bukkit.getServer().messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord")
        Bukkit.getServer().messenger.unregisterOutgoingPluginChannel(plugin, "BungeeCord")
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "BungeeCord") {
            return
        }
        if (targetServer.get().isNotEmpty()) return
        val `in` = ByteStreams.newDataInput(message)
        val subchannel = `in`.readUTF()
        if ("ChromaCore-Spawn" == subchannel) {
            // Use the code sample in the 'Response' sections below to read
            // the data.
            val len = `in`.readShort()
            val msgbytes = ByteArray(len.toInt())
            `in`.readFully(msgbytes)
            try {
                val msgin = DataInputStream(ByteArrayInputStream(msgbytes))
                val somedata = msgin.readUTF() // Read the data in the same way you wrote it
                if ("SendToSpawn" != somedata) {
                    println("somedata: $somedata")
                    return
                }
                player.teleport(spawnloc!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else println("Subchannel: $subchannel")
    }

    /**
     * The BungeeCord server that has the spawn. Set to empty if this server is the target.
     */
    private val targetServer get() = config.getData("targetServer", "")
    private var spawnloc: Location? = null

    @CommandClass(helpText = ["Spawn", "Teleport to spawn."])
    inner class SpawnCommand : ICommand2MC() {
        @Subcommand
        fun def(player: Player) {
            if (targetServer.get().isEmpty()) {
                player.sendMessage("\${ChatColor.AQUA}Teleporting to spawn...")
                try {
                    if (MainPlugin.ess != null) MainPlugin.ess!!.getUser(player).teleport
                        .teleport(spawnloc, Trade(BigDecimal.ZERO, MainPlugin.ess), PlayerTeleportEvent.TeleportCause.COMMAND) else player.teleport(spawnloc!!)
                } catch (e: Exception) {
                    player.sendMessage("\${ChatColor.RED}Failed to teleport: $e")
                }
                return
            }
            val out = ByteStreams.newDataOutput()
            out.writeUTF("Connect")
            out.writeUTF(targetServer.get())
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
            Bukkit.getScheduler().runTask(plugin, Runnable {
                //Delay it a bit
                val outt = ByteStreams.newDataOutput()
                outt.writeUTF("ForwardToPlayer") // So BungeeCord knows to forward it
                outt.writeUTF(player.name)
                outt.writeUTF("ChromaCore-Spawn") // The channel name to check if this your data
                val msgbytes = ByteArrayOutputStream()
                val msgout = DataOutputStream(msgbytes)
                try {
                    msgout.writeUTF("SendToSpawn") // You can do anything you want with msgout
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
                outt.writeShort(msgbytes.toByteArray().size)
                outt.write(msgbytes.toByteArray())
                player.sendPluginMessage(plugin, "BungeeCord", outt.toByteArray())
            })
        }
    }
}
