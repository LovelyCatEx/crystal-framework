package com.lovelycatv.crystalframework.shared.service.redis

import org.springframework.data.redis.core.*
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class RedisServiceImpl(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>
) : RedisService {
    override fun hasKey(key: String): Mono<Boolean> {
        return this.reactiveRedisTemplate.hasKey(key)
    }

    override fun removeKey(vararg key: String): Mono<Long> {
        return this.reactiveRedisTemplate.delete(*key)
    }

    override fun <T: Any> opsForValue(): ReactiveValueOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForValue() as ReactiveValueOperations<String, T>
    }

    override fun <T: Any> get(key: String): Mono<T> {
        return this.opsForValue<T>()
            .get(key)
    }

    override fun <T: Any> set(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return if (duration != null) {
            this.opsForValue<T>().set(key, value, duration)
        } else {
            this.opsForValue<T>().set(key, value)
        }
    }

    override fun <T: Any> setIfAbsent(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return if (duration != null) {
            this.opsForValue<T>().setIfAbsent(key, value, duration)
        } else {
            this.opsForValue<T>().setIfAbsent(key, value)
        }
    }

    override fun <T: Any> setIfPresent(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return if (duration != null) {
            this.opsForValue<T>().setIfPresent(key, value, duration)
        } else {
            this.opsForValue<T>().setIfPresent(key, value)
        }
    }

    override fun <T: Any> setBit(key: String, offset: Long, value: Boolean): Mono<Boolean> {
        return this.opsForValue<T>().setBit(key, offset, value)
    }

    override fun <K: Any, V: Any> opsForHash(): ReactiveHashOperations<String, K, V> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForHash<K, V>() as ReactiveHashOperations<String, K, V>
    }

    override fun <T: Any> opsForList(): ReactiveListOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForList() as ReactiveListOperations<String, T>
    }

    override fun <T: Any> opsForSet(): ReactiveSetOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForSet() as ReactiveSetOperations<String, T>
    }

    override fun <T: Any> opsForZSet(): ReactiveZSetOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForZSet() as ReactiveZSetOperations<String, T>
    }

    override fun <T: Any> opsForGeo(): ReactiveGeoOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForGeo() as ReactiveGeoOperations<String, T>
    }

    override fun <T: Any> opsForHyperLogLog(): ReactiveHyperLogLogOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForHyperLogLog() as ReactiveHyperLogLogOperations<String, T>
    }
}