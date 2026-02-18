package com.lovelycatv.template.springboot.shared.service.redis

import org.springframework.data.redis.core.*
import reactor.core.publisher.Mono
import java.time.Duration

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
}