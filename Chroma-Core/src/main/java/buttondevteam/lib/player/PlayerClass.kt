package buttondevteam.lib.player

/**
 * Specifies a [TBMCPlayerBase] direct subclass. For Minecraft data, use [UserClass]
 * 
 * @author NorbiPeti
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlayerClass(
    /**
     * Indicates the plugin's name which this player class belongs to. Used to create a section for each plugin.
     */
    val pluginname: String
)
