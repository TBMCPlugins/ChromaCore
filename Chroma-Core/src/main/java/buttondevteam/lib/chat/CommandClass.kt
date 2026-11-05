package buttondevteam.lib.chat

import java.lang.annotation.Inherited

/**
 * **Abstract classes with no [CommandClass] annotations will be ignored.** Classes that are not abstract or have the annotation will be included in the command path unless
 * [.excludeFromPath] is true.<br></br>
 * *All commands with no modOnly set will <u>not be mod only</u>*
 * 
 * @author NorbiPeti
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class CommandClass(
    /**
     * Determines whether the command can only be used by mods and above or regular players can use it as well.<br></br>
     * **If not set, the command will <u>not</u> be mod only**
     * 
     * @return If the command is mod only
     */
    val modOnly: Boolean = false, // TODO: This doesn't seem to be used
    /**
     * The command's path, or name if top-level command.<br></br>
     * For example:<br></br>
     * "u admin updateplugin" or "u" for the top level one<br></br>
     * <u>The path must be lowercase!</u><br></br>
     * 
     * @return The command path, *which is the command class name by default* (removing any "command" from it)
     */
    val path: String = "",
    /**
     * Exclude this class from the path. Useful if more commands share some property but aren't subcommands of a common command. See [CommandClass] for more details.
     */
    val excludeFromPath: Boolean = false,
    /**
     * The help text to show for the players. A usage message will be also shown below it.<br></br>
     * **The fist line will be converted to a header.**
     * 
     * @return The help text
     */
    val helpText: Array<String> = [],
    /**
     * The main permission which allows using this command (individual access can be still granted with "chroma.command.X").
     * Used to be "tbmc.admin"
     */
    val permGroup: String = "" //TODO: A single annotation instead of these two
)
