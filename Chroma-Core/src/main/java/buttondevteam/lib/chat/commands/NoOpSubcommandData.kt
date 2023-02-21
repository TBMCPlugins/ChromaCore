package buttondevteam.lib.chat.commands

open class NoOpSubcommandData(
    /**
     * Custom help text that depends on the context. Overwrites the static one.
     * The function receives the sender as the command itself receives it.
     */
    private val helpTextGetter: (Any) -> Array<String>
) {
    /**
     * Get help text for this subcommand. Returns an empty array if it's not specified.
     *
     * @param sender The sender running the command
     * @return Help text shown to the user
     */
    fun getHelpText(sender: Any): Array<String> {
        return helpTextGetter(sender)
    }
}