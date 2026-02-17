package com.lovelycatv.template.springboot.shared.service.redis

import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.data.redis.core.ReactiveGeoOperations
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveHyperLogLogOperations
import org.springframework.data.redis.core.ReactiveListOperations
import org.springframework.data.redis.core.ReactiveSetOperations
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.data.redis.core.ReactiveZSetOperations
import reactor.core.publisher.Mono
import java.time.Duration

interface RedisService {
    fun hasKey(key: String): Mono<Boolean>

    fun removeKey(vararg key: String): Mono<Long>

    fun <T> opsForValue(): ReactiveValueOperations<String, T>

    fun <T> get(key: String): Mono<T?>

    fun <T> set(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T> setIfAbsent(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T> setIfPresent(key: String, value: T, duration: Duration? = null): Mono<Boolean>

    fun <T> setBit(key: String, offset: Long, value: Boolean): Mono<Boolean>

    fun <K, V> opsForHash(): ReactiveHashOperations<String, K, V>

    fun <T> opsForList(): ReactiveListOperations<String, T>

    fun <T> opsForSet(): ReactiveSetOperations<String, T>

    fun <T> opsForZSet(): ReactiveZSetOperations<String, T>

    fun <T> opsForGeo(): ReactiveGeoOperations<String, T>

    fun <T> opsForHyperLogLog(): ReactiveHyperLogLogOperations<String, T>

    fun <T> toKVStore(clazz: Class<T>): ExpiringKVStore<String, T>
}