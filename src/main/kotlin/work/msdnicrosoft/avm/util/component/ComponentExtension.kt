package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.Component
import java.util.Optional

fun Optional<Component>.orEmpty(): Component = this.orElse(Component.empty())
