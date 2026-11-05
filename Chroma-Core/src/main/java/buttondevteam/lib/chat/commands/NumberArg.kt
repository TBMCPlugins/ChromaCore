package buttondevteam.lib.chat.commands

/**
 * A command argument that can have a number as a value.
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class NumberArg(
    /**
     * The highest value that can be used for this argument.
     */
    val upperLimit: Double = Double.POSITIVE_INFINITY,
    /**
     * The lowest value that can be used for this argument.
     */
    val lowerLimit: Double = Double.NEGATIVE_INFINITY
)
