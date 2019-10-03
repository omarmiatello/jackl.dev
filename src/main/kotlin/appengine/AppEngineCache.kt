package com.github.jacklt.gae.ktor.tg.appengine

import com.google.appengine.api.memcache.Expiration
import com.google.appengine.api.memcache.MemcacheServiceFactory
import kotlinx.serialization.KSerializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object LocalCache {
    private val cache = mutableMapOf<String, Pair<Long, String>>()

    operator fun get(key: String) =
            cache[key]?.let { (expireInMills, value) -> value.takeIf { System.currentTimeMillis() < expireInMills } }

    operator fun set(key: String, expireInMills: Long = System.currentTimeMillis() + 5 * 60000, value: String) {
        cache[key] = expireInMills to value
    }

    fun delete(key: String) {
        cache.remove(key)
    }

    fun clear() = cache.clear()
}

class AppEngineCache(
        val defaultExpiration: Expiration = Expiration.byDeltaSeconds(3600),
        val useLocalCache: Boolean = false
) {
    val memcache by lazy { MemcacheServiceFactory.getMemcacheService() }

    operator fun get(key: String): String? {
        val localData = if (useLocalCache) LocalCache[key] else null
        return localData ?: (memcache.get(key) as String?)?.also { LocalCache[key] = it }
    }

    operator fun set(key: String, value: String) = memcache.put(key, value, defaultExpiration)
            .also { LocalCache[key] = value }

    fun delete(key: String) = memcache.delete(key)
        .also { LocalCache.delete(key) }

    inline fun getOrPut(key: String, defaultValue: () -> String) =
            get(key) ?: defaultValue().also { set(key, it) }

    fun clear() {
        memcache.clearAll()
        LocalCache.clear()
    }
}

val appEngineCacheFast = AppEngineCache(useLocalCache = true)

fun cacheString(defaultValue: () -> String) =
        object : ReadOnlyProperty<Any, String> {
            override fun getValue(thisRef: Any, property: KProperty<*>) =
                    appEngineCacheFast.getOrPut("${thisRef.javaClass.canonicalName}.${property.name}", defaultValue)
        }

fun <T> cacheObject(serializer: KSerializer<T>, defaultValue: () -> T) =
        object : ReadOnlyProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                val string = appEngineCacheFast.getOrPut("${thisRef.javaClass.canonicalName}.${property.name}") {
                    json.stringify(serializer, defaultValue())
                }
                return json.parse(serializer, string)
            }
        }

