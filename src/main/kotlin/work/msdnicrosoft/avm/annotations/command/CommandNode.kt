package work.msdnicrosoft.avm.annotations.command

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommandNode(
    val name: String,
    vararg val arguments: String
)
