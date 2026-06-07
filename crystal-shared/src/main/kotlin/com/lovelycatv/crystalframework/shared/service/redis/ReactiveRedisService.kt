package com.lovelycatv.crystalframework.shared.service.redis

import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.redis.core.*
import reactor.core.publisher.Mono
import java.time.Duration

interface ReactiveRedisService {
    fun hasKey(key: String): Mono<Boolean>

    fun removeKey(vararg key: String): Mono<Long>

    fun <T: Any> opsForValue(): ReactiveValueOperations<String, T>

    fun <T: Any> get(key: String): Mono<T>

    fun <T: Any> set(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T: Any> setIfAbsent(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T: Any> setIfPresent(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T: Any> setBit(key: String, offset: Long, value: Boolean): Mono<Boolean>

    fun <K: Any, V: Any> opsForHash(): ReactiveHashOperations<String, K, V>

    fun <T: Any> opsForList(): ReactiveListOperations<String, T>

    fun <T: Any> opsForSet(): ReactiveSetOperations<String, T>

    fun <T: Any> opsForZSet(): ReactiveZSetOperations<String, T>

    fun <T: Any> opsForGeo(): ReactiveGeoOperations<String, T>

    fun <T: Any> opsForHyperLogLog(): ReactiveHyperLogLogOperations<String, T>

    fun <T: Any> asReactiveKVStore(): ReactiveExpiringKVStore<String, T> {
        return object : ReactiveExpiringKVStore<String, T> {
            override suspend fun set(key: String, value: T, expirationInMs: Long) {
                opsForValue<T>()
                    .set(key, value, Duration.ofMillis(expirationInMs))
                    .awaitFirstOrNull()
            }

            override suspend fun set(key: String, value: T) {
                opsForValue<T>()
                    .set(key, value)
                    .awaitFirstOrNull()
            }

            override suspend fun containsKey(key: String): Boolean {
                return hasKey(key).awaitFirstOrNull() == true
            }

            override suspend fun get(key: String): T? {
                return this@ReactiveRedisService
                    .get<T>(key)
                    .awaitFirstOrNull()
            }

            override suspend fun remove(key: String): T? {
                val v = get(key)

                if (v != null) {
                    this@ReactiveRedisService.removeKey(key).awaitFirstOrNull()
                }

                return v
            }
        }
    }
}