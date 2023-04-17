package buttondevteam.lib.chat

import buttondevteam.lib.chat.Command2.Subcommand
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.Function

/**
 * This class is used as a base class for all the specific command implementations.
 * It primarily holds information about the command itself and how it should be run, ideally in a programmer-friendly way.
 * Any inferred and processed information about this command will be stored in the command manager (Command2*).
 *
 * @param TP The sender's type
</TP> */
abstract class ICommand2<TP : Command2Sender>(manager: Command2<*, TP>) {
    /**
     * Default handler for commands, can be used to copy the args too.
     *
     * @param sender The sender which ran the command
     * @return The success of the command
     */
    @Suppress("UNUSED_PARAMETER")
    fun def(sender: TP): Boolean {
        return false
    }

    /**
     * Convenience method. Return with this.
     *
     * @param sender  The sender of the command
     * @param message The message to send to the sender
     * @return Always true so that the usage isn't shown
     */
    protected fun respond(sender: TP, message: String): Boolean {
        sender.sendMessage(message)
        return true
    }

    /**
     * Return null to not add any help text, return an empty array to only print subcommands.<br></br>
     * By default, returns null if the Subcommand annotation is not present and returns an empty array if no help text can be found.
     *
     * @param method The method of the subcommand
     * @return The help text, empty array or null
     */
    open fun getHelpText(method: Method, ann: Subcommand): Array<String> {
        val cc = javaClass.getAnnotation(CommandClass::class.java)
        return if (ann.helpText.isNotEmpty() || cc == null) ann.helpText else cc.helpText //If cc is null then it's empty array
    }

    private val path: String
    val manager: Command2<*, TP>
    open val commandPath: String
        /**
         * The command's path, or name if top-level command.<br></br>
         * For example:<br></br>
         * "u admin updateplugin" or "u" for the top level one<br></br>
         * <u>The path must be lowercase!</u><br></br>
         *
         * @return The command path, *which is the command class name by default* (removing any "command" from it) - Change via the [CommandClass] annotation
         */
        get() = path

    init {
        path = getcmdpath()
        this.manager = manager
    }

    open val commandPaths: Array<String>
        /**
         * All of the command's paths it will be invoked on. Does not include aliases or the default path.
         * Must be lowercase and must include the full path.
         *
         * @return The full command paths that this command should be registered under in addition to the default one.
         */
        get() =// TODO: Deal with this (used for channel IDs)
            EMPTY_PATHS

    private fun getcmdpath(): String {
        if (!javaClass.isAnnotationPresent(CommandClass::class.java)) throw RuntimeException(
            "No @CommandClass annotation on command class " + javaClass.simpleName + "!"
        )
        val getFromClass = Function { cl: Class<*> ->
            cl.simpleName.lowercase(Locale.getDefault()).replace("commandbase", "") // <-- ...
                .replace("command", "")
        }
        var path = javaClass.getAnnotation(CommandClass::class.java).path
        var prevpath = if (path.isEmpty()) getFromClass.apply(javaClass) else path.also { path = it }
        var cl: Class<*>? = javaClass.superclass
        while (cl != null && cl.getPackage().name != ICommand2MC::class.java.getPackage().name) {
            //
            var newpath: String
            val ccann: CommandClass? = cl.getAnnotation(CommandClass::class.java)
            if (ccann?.path.isNullOrEmpty() || ccann?.path == prevpath) {
                if (ccann?.excludeFromPath ?: Modifier.isAbstract(cl.modifiers)) {
                    cl = cl.superclass
                    continue
                }
                newpath = getFromClass.apply(cl)
            } else newpath = ccann!!.path
            path = "$newpath $path"
            prevpath = newpath
            cl = cl.superclass
        }
        return path
    }

    companion object {
        private val EMPTY_PATHS = emptyArray<String>()
    }
}