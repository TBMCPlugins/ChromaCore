package buttondevteam.lib.chat

import buttondevteam.lib.chat.Command2.Subcommand
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * This class is used as a base class for all the specific command implementations.
 * It primarily holds information about the command itself and how it should be run, ideally in a programmer-friendly way.
 * Any inferred and processed information about this command will be stored in the command manager (Command2*).
 *
 * @param TP The sender's type
</TP> */
abstract class ICommand2<TP : Command2Sender>(val manager: Command2<*, TP>) {
    /**
     * Default handler for commands, can be used to copy the args too.
     *
     * @param sender The sender which ran the command
     * @return The success of the command
     */
    open fun def(sender: TP): Boolean {
        return false
    }

    /**
     * Convenience method. Return with this.
     *
     * @param sender  The sender of the command
     * @param message The message to send to the sender
     * @return Always true so that the usage isn't shown
     */
    @Suppress("unused")
    protected fun respond(sender: TP, message: String): Boolean {
        sender.sendMessage(message)
        return true
    }

    /**
     * Return null to not add any help text, return an empty array to only print subcommands.
     *
     * By default, returns null if the Subcommand annotation is not present and returns an empty array if no help text can be found.
     *
     * @param method The method of the subcommand
     * @return The help text, empty array or null
     */
    open fun getHelpText(method: Method, ann: Subcommand): Array<String> {
        val cc = javaClass.getAnnotation(CommandClass::class.java)
        return if (ann.helpText.isNotEmpty() || cc == null) ann.helpText else cc.helpText //If cc is null then it's empty array
    }

    /**
     * The command's path, or name if top-level command.
     *
     * For example:
     *
     * "u admin updateplugin" or "u" for the top level one
     *
     * __The path must be lowercase!__
     *
     * @return The command path, *which is the command class name by default* (removing any "command" from it) - Change via the [CommandClass] annotation
     */
    open val commandPath: String = getcmdpath()

    open val commandPaths: Array<String>
        /**
         * All of the command's paths it will be invoked on. Does not include aliases or the default path.
         * Must be lowercase and must include the full path.
         *
         * @return The full command paths that this command should be registered under in addition to the default one.
         */
        get() = EMPTY_PATHS // TODO: Deal with this (used for channel IDs)

    private fun getcmdpath(): String {
        if (!javaClass.isAnnotationPresent(CommandClass::class.java))
            throw RuntimeException("No @CommandClass annotation on command class ${javaClass.simpleName}!")
        val classList = mutableListOf<Class<*>>(javaClass)
        while (true) {
            val superClass = classList.last().superclass
            if (superClass != null && superClass.getPackage().name != ICommand2MC::class.java.getPackage().name) {
                classList.add(superClass)
            } else {
                break
            }
        }
        return classList.reversed().associateWith { it.getAnnotation(CommandClass::class.java) }
            .mapNotNull {
                if (it.value?.path.isNullOrEmpty())
                    if (it.value?.excludeFromPath ?: Modifier.isAbstract(it.key.modifiers))
                        null
                    else
                        it.key.simpleName.lowercase().removeSuffix("commandbase").removeSuffix("command")
                else
                    it.value.path
            }.joinToString(" ")
    }

    companion object {
        private val EMPTY_PATHS = emptyArray<String>()
    }
}