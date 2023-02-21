package buttondevteam.lib.chat

import buttondevteam.lib.chat.commands.NoOpSubcommandData
import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import java.util.function.Predicate

class CoreCommandNode<T : Command2Sender, TC : ICommand2<*>, TSD : NoOpSubcommandData>(
    literal: String,
    command: Command<T>,
    requirement: Predicate<T>,
    redirect: CommandNode<T>,
    modifier: RedirectModifier<T>,
    forks: Boolean,
    val data: TSD
) : LiteralCommandNode<T>(literal, command, requirement, redirect, modifier, forks)