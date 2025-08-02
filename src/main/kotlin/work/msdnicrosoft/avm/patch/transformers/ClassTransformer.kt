package work.msdnicrosoft.avm.patch.transformers

import java.lang.instrument.ClassFileTransformer

interface ClassTransformer : ClassFileTransformer {
    val targetClass: Class<*>
    fun shouldTransform(): Boolean
}
