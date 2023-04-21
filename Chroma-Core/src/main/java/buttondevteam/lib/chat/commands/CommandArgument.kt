package buttondevteam.lib.chat.commands

/**
 * A command argument's information to be used to construct the command.
 */
class CommandArgument(
    val name: String, // TODO: Remove <> from name and add it where appropriate
    val type: Class<*>,
    val greedy: Boolean,
    val limits: Pair<Double, Double>,
    val optional: Boolean,
    val description: String,
    val annotations: Array<Annotation>
)
