package work.msdnicrosoft.avm.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RootCommand(val name: String)
