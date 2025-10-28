package work.msdnicrosoft.avm.annotations.command

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RootCommand(val name: String)
