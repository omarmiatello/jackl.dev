package com.github.omarmiatello.jackldev.utils

object InMemoryCache {
    private val cache = mutableMapOf<String, Pair<Long, String>>()

    operator fun get(key: String) =
        cache[key]?.let { (expireInMills, value) -> value.takeIf { System.currentTimeMillis() < expireInMills } }

    inline fun getOrPut(
        key: String,
        expireInMills: Long = System.currentTimeMillis() + 5 * 60000,
        defaultValue: () -> String
    ) =
        get(key) ?: defaultValue().also {
            set(
                key,
                expireInMills,
                it
            )
        }

    operator fun set(key: String, expireInMills: Long = System.currentTimeMillis() + 5 * 60000, value: String) {
        cache[key] = expireInMills to value
    }

    fun delete(key: String) {
        cache.remove(key)
    }

    fun clear() = cache.clear()
}

