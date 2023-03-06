package buttondevteam.lib.chat.commands

import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class MCCommandSettings(
    /**
     * The main permission which allows using this command (individual access can be still revoked with "chroma.command.X").
     * Used to be "tbmc.admin". The [.MOD_GROUP] is provided to use with this.
     */
    val permGroup: String = "",
    /**
     * Whether the (sub)command is mod only. This means it requires the chroma.mod permission.
     * This is just a shorthand for providing MOD_GROUP for permGroup.
     */
    val modOnly: Boolean = false
) {
    companion object {
        /**
         * Allowed for OPs only by default
         */
        const val MOD_GROUP = "mod"
    }
}
