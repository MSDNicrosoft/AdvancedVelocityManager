package work.msdnicrosoft.avm.util

object ReflectUtil {

    fun getField(clazz: Class<*>, field: String) = try {
        clazz.getDeclaredField(field).apply { trySetAccessible() }
    } catch (_: NoSuchFieldException) {
        error("Failed to get field $field of class ${clazz.name}")
    }

    fun getField(className: String, field: String) = getField(Class.forName(className), field)
}
