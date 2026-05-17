package com.lovelycatv.crystalframework.shared.service.redis

import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.data.redis.core.*
import java.time.Duration

interface RedisService {
    fun hasKey(key: String): Boolean

    fun removeKey(vararg key: String): Long

    fun <T : Any> opsForValue(): ValueOperations<String, T>

    fun <T : Any> get(key: String): T?

    fun <T : Any> set(
        key: String,
        value: T,
        duration: Duration? = null
    )

    fun <T : Any> setIfAbsent(
        key: String,
        value: T,
        duration: Duration? = null
    ): Boolean

    fun <T : Any> setIfPresent(
        key: String,
        value: T,
        duration: Duration? = null
    ): Boolean

    fun <T : Any> setBit(
        key: String,
        offset: Long,
        value: Boolean
    ): Boolean

    fun <K : Any, V : Any> opsForHash(): HashOperations<String, K, V>

    fun <T : Any> opsForList(): ListOperations<String, T>

    fun <T : Any> opsForSet(): SetOperations<String, T>

    fun <T : Any> opsForZSet(): ZSetOperations<String, T>

    fun <T : Any> opsForGeo(): GeoOperations<String, T>

    fun <T : Any> opsForHyperLogLog(): HyperLogLogOperations<String, T>

    fun <T : Any> asKVStore(): ExpiringKVStore<String, T> {
        return object : ExpiringKVStore<String, T> {

            override fun set(
                key: String,
                value: T,
                expiration: Long
            ) {
                this@RedisService.set(
                    key,
                    value,
                    Duration.ofMillis(expiration)
                )
            }

            override fun set(key: String, value: T) {
                this@RedisService.set(key, value)
            }

            override fun containsKey(key: String): Boolean {
                return this@RedisService.hasKey(key)
            }

            override fun get(key: String): T? {
                return this@RedisService.get(key)
            }

            override fun remove(key: String): T? {
                val value = get(key)

                if (value != null) {
                    this@RedisService.removeKey(key)
                }

                return value
            }

            override val keys: Set<String>
                get() = emptySet()

            override val size: Int
                get() = keys.size

            override fun clear() {
                throw UnsupportedOperationException(
                    "Redis cannot be cleared"
                )
            }
        }
    }
}