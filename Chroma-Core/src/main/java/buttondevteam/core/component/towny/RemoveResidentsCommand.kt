package buttondevteam.core.component.towny

import buttondevteam.core.MainPlugin
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabComplete
import buttondevteam.lib.chat.ICommand2MC
import com.palmergames.bukkit.towny.TownySettings
import com.palmergames.bukkit.towny.TownyUniverse
import com.palmergames.bukkit.towny.`object`.Resident
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import java.util.function.Consumer

@CommandClass(
    path = "chroma remresidents",
    modOnly = true,
    helpText = ["Removes invalid Towny residents from their towns (usually after a rename that didn't get caught by the plugin)", "If the delete eco account setting is off, then it will completely delete the resident", "(The economy account could still be used by the player)"]
)
class RemoveResidentsCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: CommandSender, @OptionalArg @CustomTabComplete("remove") remove: String?) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            sender.sendMessage("Starting...")
            val ds = TownyUniverse.getInstance().dataSource
            val res: Map<Resident, OfflinePlayer> = ds.residents
                .mapNotNull { r ->
                    val player = MainPlugin.ess?.getOfflineUser(r.name)?.base ?: Bukkit.getOfflinePlayer(r.name)
                    if (player.hasPlayedBefore()) null else r to player
                }.associate { it }
            if (MainPlugin.ess == null)
                sender.sendMessage("${ChatColor.RED}Essentials not found, players who haven't joined after changing their names are also listed here.")
            sender.sendMessage("Residents to remove:")
            res.values.forEach { op: OfflinePlayer -> sender.sendMessage(op.name!!) }
            if (TownySettings.isDeleteEcoAccount()) sender.sendMessage("${ChatColor.AQUA}Will only remove from town, as delete eco account setting is on") else sender.sendMessage(
                "${ChatColor.YELLOW}Will completely delete the resident, delete eco account setting is off"
            )
            if (remove != null && remove.equals("remove", ignoreCase = true)) {
                sender.sendMessage("Removing residents...") //Removes from town and deletes town if needed - doesn't delete the resident if the setting is on
                //If it did, that could mean the player's economy is deleted too, unless this setting is false
                res.keys.forEach(if (TownySettings.isDeleteEcoAccount())
                    Consumer { resident -> ds.removeResident(resident) }
                else
                    Consumer { resident -> ds.removeResidentList(resident) })
                sender.sendMessage("Done")
            }
        })
    }
}