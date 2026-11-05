package buttondevteam.lib.chat

/**
 * Can be used if an argument should be completed with predefined strings.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomTabComplete(vararg val value: String)
