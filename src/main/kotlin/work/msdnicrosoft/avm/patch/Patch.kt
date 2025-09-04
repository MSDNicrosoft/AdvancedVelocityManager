/**
 * Portions of this code are modified from cancellable-chat and are licensed under the MIT License (MIT).
 *
 * https://github.com/ZhuRuoLing/cancellable-chat/blob/977f1dfef71d783b0a824e80ab36ce25d30f2e65
 * /src/main/java/icu/takeneko/cancellablechat/InstrumentationAccess.java
 *
 * Copyright (c) 2024 竹若泠
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package work.msdnicrosoft.avm.patch

import net.bytebuddy.agent.ByteBuddyAgent
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.patch.transformers.ClassTransformer
import work.msdnicrosoft.avm.patch.transformers.KeyedChatHandlerTransformer
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory

object Patch {
    private val transformers: Set<ClassTransformer> = setOf(
        KeyedChatHandlerTransformer,
    )

    private lateinit var instrumentation: Instrumentation

    /**
     * Initializes the instrumentation object and adds a transformer to the JVM.
     *
     * This function should be called once at startup to initialize the instrumentation object and add
     * a transformer to the JVM.
     *
     */
    fun init() {
        try {
            val toTransform: List<ClassTransformer> = this.transformers.filter { it.shouldTransform() }

            if (toTransform.isEmpty()) return

            this.instrumentation = ByteBuddyAgent.install()
            this.warnIfDynamicAgentDisabled()

            toTransform.forEach { transformer ->
                this.instrumentation.addTransformer(transformer, true)
                this.instrumentation.retransformClasses(transformer.targetClass)
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize Patch", e)
        }
    }

    private fun warnIfDynamicAgentDisabled() {
        if ("-XX:+EnableDynamicAgentLoading" !in ManagementFactory.getRuntimeMXBean().inputArguments) {
            logger.info("Dynamic agent loading warnings detected.")
            logger.info("It is expected behavior and you can safely ignore the warnings.")
        }
    }
}
