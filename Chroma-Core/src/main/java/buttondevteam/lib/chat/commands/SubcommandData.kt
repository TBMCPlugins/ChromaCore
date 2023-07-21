package buttondevteam.lib.chat.commands

import buttondevteam.lib.chat.Command2Sender
import buttondevteam.lib.chat.ICommand2
import java.lang.reflect.Method

/**
 * Stores information about the subcommand that can be used to construct the Brigadier setup and to get information while executing the command.
 *
 * @param TC Command class type
 * @param TP Command sender type
 */
class SubcommandData<TC : ICommand2<*>, TP : Command2Sender>(
    /**
     * The type of the sender running the command.
     * The actual sender type may not be represented by Command2Sender (TP).
     * In that case it has to match the expected type.
     */
    val senderType: Class<*>,

    /**
     * Command arguments collected from the subcommand method.
     * Used to construct the arguments for Brigadier and to hold extra information.
     */
    val arguments: Map<String, CommandArgument>,

    /**
     * Command arguments in the order they appear in code and in game.
     */
    val argumentsInOrder: List<CommandArgument>,

    /**
     * The original command class that this data belongs to.
     */
    val command: TC,
    /**
     * Custom help text that depends on the context. Overwrites the static one.
     * The function receives the sender as the command itself receives it.
     */
    helpTextGetter: (Any) -> Array<String>,

    /**
     * A function that determines whether the user has permission to run this subcommand.
     */
    private val permissionCheck: (TP, SubcommandData<TC, TP>) -> Boolean,

    /**
     * All annotations implemented by the method that executes the command. Can be used to add custom metadata when implementing a platform.
     */
    val annotations: Array<Annotation>,

    /**
     * The space-separated full command path of this subcommand.
     */
    val fullPath: String,

    /**
     * The method to run when executing the command.
     */
    private val method: Method
) : NoOpSubcommandData(helpTextGetter) {

    /**
     * Check if the user has permission to execute this subcommand.
     *
     * @param sender The sender running the command
     * @return Whether the user has permission
     */
    fun hasPermission(sender: TP): Boolean {
        return permissionCheck(sender, this)
    }

    /**
     * Execute the command and return the result. Doesn't perform any checks.
     *
     * @param sender The actual sender as expected by the method
     * @param args The rest of the method args
     */
    fun executeCommand(sender: Any, vararg args: Any?): Any? {
        method.isAccessible = true
        return method.invoke(command, sender, *args)
    }

    /**
     * Send the help text to the specified sender.
     */
    fun sendHelpText(sender: TP) {
        sender.sendMessage(getHelpText(sender))
    }
}