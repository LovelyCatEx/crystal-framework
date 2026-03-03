package com.lovelycatv.crystalframework.shared.service.redis

import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.data.redis.core.*
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

interface RedisService {
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

    fun <T: Any> asKVStore(): ExpiringKVStore<String, T> {
        val executor = Executors.newSingleThreadExecutor { r ->
            Thread(r, "redis-blocking-thread").apply { isDaemon = true }
        }

        return object : ExpiringKVStore<String, T> {
            override fun set(key: String, value: T, expiration: Long) {
                CompletableFuture.runAsync({
                    opsForValue<T>()
                        .set(key, value, Duration.ofMillis(expiration))
                        .block()
                }, executor).join()
            }

            override fun set(key: String, value: T) {
                CompletableFuture.runAsync({
                    opsForValue<T>()
                        .set(key, value)
                        .block()
                }, executor).join()
            }

            override fun containsKey(key: String): Boolean {
                return CompletableFuture.supplyAsync({
                    hasKey(key).block() == true
                }, executor).join()
            }

            override fun get(key: String): T? {
                return CompletableFuture.supplyAsync({
                    this@RedisService
                        .get<T>(key)
                        .block()
                }, executor).join()
            }

            override fun remove(key: String): T? {
                return CompletableFuture.supplyAsync({
                    val v = get(key)
                    if (v != null) {
                        this@RedisService.removeKey(key).block()
                    }
                    v
                }, executor).join()
            }

            override val keys: Set<String>
                get() = emptySet()
            override val size: Int
                get() = keys.size

            override fun clear() {
            }
        }
    }
}