package work.msdnicrosoft.avm.util.reflect

import java.lang.reflect.AnnotatedElement

inline fun <reified A : Annotation> AnnotatedElement.getAnnotationIfPresent(): A? =
    try {
        getAnnotation(A::class.java)
    } catch (_: NullPointerException) {
        null
    }
