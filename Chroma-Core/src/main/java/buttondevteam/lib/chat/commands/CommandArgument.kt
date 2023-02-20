package buttondevteam.lib.chat.commands

/**
 * A command argument's information to be used to construct the command.
 */
class CommandArgument(
    val name: String,
    val type: Class<*>,
    val greedy: Boolean,
    val limits: Pair<Double, Double>,
    val optional: Boolean,
    val description: String
)
