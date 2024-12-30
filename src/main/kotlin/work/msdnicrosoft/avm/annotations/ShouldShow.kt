package work.msdnicrosoft.avm.annotations

/**
 * Whether a field should be displayed in the help message of the command.
 *
 * @property arguments The arguments of the field.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ShouldShow(vararg val arguments: String = [])
