package buttondevteam.lib.chat

/**
 * The method must return with [String][] or [Iterable]&lt;[String]&gt; and may have the sender and preceding arguments as parameters.
 * The predecing arguments must be in order, from first to whatever is needed. If the nth arg is needed, you need to specify n params.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomTabCompleteMethod(
    /**
     * The parameter's name where we want to give completion
     */
    val param: String,
    /**
     * The subcommand(s) which have the parameter, by default the method's name
     */
    val subcommand: Array<String> = [],
    /**
     * Parameter types can provide tab completions. This allows disabling that.
     */
    val ignoreTypeCompletion: Boolean = false
)
