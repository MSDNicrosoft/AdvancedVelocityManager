package work.msdnicrosoft.avm.util.reflect

import com.highcapable.kavaref.extension.classOf
import java.lang.reflect.AnnotatedElement

inline fun <reified A : Annotation> AnnotatedElement.getAnnotationIfPresent(): A? =
    try {
        getAnnotation(classOf<A>())
    } catch (_: NullPointerException) {
        null
    }
