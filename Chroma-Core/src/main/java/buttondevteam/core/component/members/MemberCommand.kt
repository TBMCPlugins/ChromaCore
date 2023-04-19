package buttondevteam.core.component.members

import buttondevteam.core.MainPlugin
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.commands.MCCommandSettings
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

@CommandClass(
    path = "member", helpText = [ //
        "Member command",  //
        "Add or remove server members."]
)
class MemberCommand() : ICommand2MC() {
    @Subcommand
    @MCCommandSettings(permGroup = MCCommandSettings.MOD_GROUP)
    fun add(sender: CommandSender, player: OfflinePlayer): Boolean {
        return addRemove(sender, player, true)
    }

    @Subcommand
    @MCCommandSettings(permGroup = MCCommandSettings.MOD_GROUP)
    fun remove(sender: CommandSender, player: OfflinePlayer): Boolean {
        return addRemove(sender, player, false)
    }

    private fun addRemove(sender: CommandSender, op: OfflinePlayer, add: Boolean): Boolean {
        Bukkit.getScheduler().runTaskAsynchronously(MainPlugin.instance, Runnable {
            val component = component as MemberComponent
            if (!op.hasPlayedBefore()) {
                sender.sendMessage("${ChatColor.RED}Cannot find player or haven't played before.")
                return@Runnable
            }
            if (if (add) MainPlugin.permission.playerAddGroup(null, op, component.memberGroup.get())
                else MainPlugin.permission.playerRemoveGroup(null, op, component.memberGroup.get())
            )
                sender.sendMessage("${ChatColor.AQUA}${op.name} ${if (add) "added" else "removed"} as a member!")
            else sender.sendMessage("${ChatColor.RED}Failed to ${if (add) "add" else "remove"} ${op.name} as a member!")
        })
        return true
    }

    @Subcommand
    fun def(player: Player) {
        val component = component as MemberComponent
        val msg = if (!component.checkNotMember(player)) "You are a member." else {
            val pt = component.getPlayTime(player)
            val rt = component.getRegTime(player)
            if (pt == -1.0 || rt == -1L) {
                val result = component.addPlayerAsMember(player)
                if (result == null) "Can't assign member group because groups are not supported by the permissions plugin."
                else if (result) "You meet all the requirements."
                else "You should be a member but failed to add you to the group."
            } else String.format(
                "You need to play for %.2f hours total or play for %d more days to become a member.",
                pt, TimeUnit.MILLISECONDS.toDays(rt)
            )
        }
        player.sendMessage(msg)
    }
}