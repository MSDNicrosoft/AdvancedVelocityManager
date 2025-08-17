/**
 * Portions of this code are modified from cancellable-chat and are licensed under the MIT License (MIT).
 *
 * https://github.com/ZhuRuoLing/cancellable-chat/blob/977f1dfef71d783b0a824e80ab36ce25d30f2e65
 * /src/main/java/icu/takeneko/cancellablechat/ClassTransformerImpl.java
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

package work.msdnicrosoft.avm.patch.transformers

import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedChatHandler
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import java.security.ProtectionDomain

object KeyedChatHandlerTransformer : ClassTransformer {
    private const val TARGET_CLASS_NAME = "com/velocitypowered/proxy/protocol/packet/chat/keyed/KeyedChatHandler"
    private const val TARGET_METHOD_NAME = "invalidCancel"
    private const val TARGET_METHOD_DESC =
        "(Lorg/apache/logging/log4j/Logger;Lcom/velocitypowered/proxy/connection/client/ConnectedPlayer;)V"

    override val targetClass = classOf<KeyedChatHandler>()

    override fun shouldTransform(): Boolean = server.pluginManager.getPlugin("signedvelocity").isEmpty

    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray? {
        if (className != TARGET_CLASS_NAME) return null

        val node = ClassNode().apply {
            ClassReader(classfileBuffer).accept(this@apply, 0)
        }

        node.methods
            .find { method -> method.name == TARGET_METHOD_NAME && method.desc == TARGET_METHOD_DESC }
            ?.instructions?.iterator()
            ?.add(InsnNode(Opcodes.RETURN))

        return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
            .apply { node.accept(this) }
            .toByteArray()
    }
}
