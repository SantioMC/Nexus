package me.santio.nexus.ext

import me.santio.nexus.NexusPlugin.Companion.plugin
import kotlin.reflect.KClass

fun <T : Any> KClass<T>.reflect(instance: Any) = ReflectionClass(this.java, instance)

@Suppress("unused")
class ReflectionClass internal constructor(private val clazz: Class<*>, private val instance: Any) {

    fun <T : Any> get(vararg field: String, type: Class<T>): T? {
        return field.firstNotNullOfOrNull { clazz.getDeclaredField(it) }
            ?.also { it.isAccessible = true }
            ?.get(instance)
            ?.takeIf { type.isAssignableFrom(it::class.java) }
            ?.let { type.cast(it) }
    }

    inline fun <reified T : Any> get(vararg field: String) = get(*field, type = T::class.java)

    fun set(field: String, value: Any) {
        clazz.getDeclaredField(field).also { it.isAccessible = true }.set(instance, value)
    }

    fun <T : Any> call(names: List<String>, args: List<Any?>, type: Class<T>): T? {
        return clazz.declaredMethods
            .filter { it.name in names }
            .onEach { it.isAccessible = true }
            .firstOrNull { type.isAssignableFrom(it.returnType) || (it.returnType == Void.TYPE && type == Any::class.java) }
            .also { if (it == null) plugin.logger.warning("Failed to find method in ${clazz.name}: ${names.joinToString()}") }
            ?.invoke(instance, *args.toTypedArray())
            ?.let { type.cast(it) }
    }

    @JvmName("CallInline") inline fun <reified T : Any> call(names: List<String>, args: List<Any?>) = call(names, args, T::class.java)
    @JvmName("CallInlineVararg") inline fun <reified T : Any> call(vararg names: String) = call(names.toList(), emptyList(), T::class.java)
    @JvmName("CallUntyped") fun call(names: List<String>, args: List<Any?>) = call(names, args, Any::class.java)
    @JvmName("CallUntypedVararg") fun call(vararg names: String) = call(names.toList(), emptyList(), Any::class.java)

}