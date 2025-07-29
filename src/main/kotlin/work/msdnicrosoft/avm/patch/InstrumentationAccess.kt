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

import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedChatHandler
import net.bytebuddy.agent.ByteBuddyAgent
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import java.io.IOException
import java.lang.instrument.Instrumentation
import java.lang.management.ManagementFactory
import java.nio.file.Files
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.isDirectory

object InstrumentationAccess {
    /**
     * Path to the directory where transformed classes are stored.
     */
    val TRANSFORMER_OUTPUT_PATH = dataDirectory / ".patch"

    /**
     * Class object for the Velocity KeyedChatHandler class.
     */
    private val KEYED_CHAT_HANDLER_CLASS = KeyedChatHandler::class.java

    /**
     * Instrumentation object used for bytecode transformation.
     */
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
            if (isSignedVelocityInstalled()) return

            prepareOutputDirectory()
            instrumentation = ByteBuddyAgent.install()

            if ("-XX:+EnableDynamicAgentLoading" !in ManagementFactory.getRuntimeMXBean().inputArguments) {
                logger.info("Dynamic agent loading warnings detected.")
                logger.info("It is expected behavior and you can safely ignore the warnings.")
            }

            instrumentation.addTransformer(ClassTransformer, true)
            instrumentation.retransformClasses(KEYED_CHAT_HANDLER_CLASS)
        } catch (e: Exception) {
            logger.error("Failed to initialize Chat Bridge Hook", e)
        }
    }

    /**
     * Deletes the transformer output directory if it exists.
     *
     * This function deletes the transformer output directory if it exists. This is necessary because
     * the transformer output directory is used to store the transformed classes, and we need to make
     * sure that we don't have any stale or outdated classes lying around.
     *
     * @throws IOException if the directory cannot be deleted.
     */
    private fun deleteDirectory() = Files.walk(TRANSFORMER_OUTPUT_PATH)
        .sorted(Comparator.reverseOrder())
        .forEach {
            try {
                it.deleteExisting()
            } catch (e: IOException) {
                throw IOException("Failed to delete directory: $TRANSFORMER_OUTPUT_PATH", e)
            }
        }

    /**
     * Prepares the transformer output directory by deleting it if it exists.
     *
     * This function prepares the transformer output directory by deleting it if it exists. This is
     * necessary because we need to make sure that we have a clean slate before we start transforming
     * classes.
     */
    private fun prepareOutputDirectory() {
        try {
            if (TRANSFORMER_OUTPUT_PATH.isDirectory()) deleteDirectory()
        } catch (e: IOException) {
            throw IOException("Failed to prepare output directory", e)
        }
    }

    private fun isSignedVelocityInstalled() =
        server.pluginManager.getPlugin("signedvelocity").isPresent
}
