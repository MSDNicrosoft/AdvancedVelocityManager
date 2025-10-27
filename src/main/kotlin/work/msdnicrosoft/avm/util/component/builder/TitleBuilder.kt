package work.msdnicrosoft.avm.util.component.builder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.title.Title
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL
import work.msdnicrosoft.avm.util.component.builder.text.ComponentBuilder
import work.msdnicrosoft.avm.util.component.builder.text.component
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@ComponentDSL
class TitleBuilder {
    private var mainTitle: Component = Component.empty()
    private var subTitle: Component = Component.empty()

    private var fadeIn: Duration = Duration.ZERO
    private var stay: Duration = Duration.ZERO
    private var fadeOut: Duration = Duration.ZERO

    fun mainTitle(
        joinConfiguration: JoinConfiguration = JoinConfiguration.noSeparators(),
        componentBuilder: ComponentBuilder.() -> Unit
    ) {
        this.mainTitle = component(joinConfiguration, componentBuilder)
    }

    fun subTitle(
        joinConfiguration: JoinConfiguration = JoinConfiguration.noSeparators(),
        componentBuilder: ComponentBuilder.() -> Unit
    ) {
        this.subTitle = component(joinConfiguration, componentBuilder)
    }

    fun fadeIn(duration: Duration) {
        this.fadeIn = duration
    }

    fun stay(duration: Duration) {
        this.stay = duration
    }

    fun fadeOut(duration: Duration) {
        this.fadeOut = duration
    }

    fun build(): Title = Title.title(
        this.mainTitle,
        this.subTitle,
        Title.Times.times(
            this.fadeIn.toJavaDuration(),
            this.stay.toJavaDuration(),
            this.fadeOut.toJavaDuration()
        )
    )
}

inline fun title(builder: TitleBuilder.() -> Unit): Title = TitleBuilder().apply(builder).build()
