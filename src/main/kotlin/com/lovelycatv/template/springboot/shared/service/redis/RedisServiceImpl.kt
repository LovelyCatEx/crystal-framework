package com.lovelycatv.template.springboot.shared.service.redis

import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.data.redis.core.ReactiveGeoOperations
import org.springframework.data.redis.core.ReactiveHashOperations
import org.springframework.data.redis.core.ReactiveHyperLogLogOperations
import org.springframework.data.redis.core.ReactiveListOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveSetOperations
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.data.redis.core.ReactiveZSetOperations
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class RedisServiceImpl(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any?>
) : RedisService {
    override fun hasKey(key: String): Mono<Boolean> {
        return this.reactiveRedisTemplate.hasKey(key)
    }

    override fun removeKey(vararg key: String): Mono<Long> {
        return this.reactiveRedisTemplate.delete(*key)
    }

    override fun <T> opsForValue(): ReactiveValueOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForValue() as ReactiveValueOperations<String, T>
    }

    override fun <T> get(key: String): Mono<T?> {
        return this.opsForValue<T>()
            .get(key)
            .defaultIfEmpty(null)
    }

    override fun <T> set(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return if (duration != null) {
            this.opsForValue<T>().set(key, value, duration)
        } else {
            this.opsForValue<T>().set(key, value)
        }
    }

    override fun <T> setIfAbsent(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return this.opsForValue<T>().setIfAbsent(key, value, duration)
    }

    override fun <T> setIfPresent(key: String, value: T, duration: Duration?): Mono<Boolean> {
        return this.opsForValue<T>().setIfPresent(key, value, duration)
    }

    override fun <T> setBit(key: String, offset: Long, value: Boolean): Mono<Boolean> {
        return this.opsForValue<T>().setBit(key, offset, value)
    }

    override fun <K, V> opsForHash(): ReactiveHashOperations<String, K, V> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForHash<K, V>() as ReactiveHashOperations<String, K, V>
    }

    override fun <T> opsForList(): ReactiveListOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForList() as ReactiveListOperations<String, T>
    }

    override fun <T> opsForSet(): ReactiveSetOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForSet() as ReactiveSetOperations<String, T>
    }

    override fun <T> opsForZSet(): ReactiveZSetOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForZSet() as ReactiveZSetOperations<String, T>
    }

    override fun <T> opsForGeo(): ReactiveGeoOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForGeo() as ReactiveGeoOperations<String, T>
    }

    override fun <T> opsForHyperLogLog(): ReactiveHyperLogLogOperations<String, T> {
        @Suppress("UNCHECKED_CAST")
        return reactiveRedisTemplate.opsForHyperLogLog() as ReactiveHyperLogLogOperations<String, T>
    }

    private var cachedKVStores: MutableMap<Class<*>, ExpiringKVStore<String, *>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> toKVStore(clazz: Class<T>): ExpiringKVStore<String, T> {
        return cachedKVStores.getOrPut(clazz) {
            object : ExpiringKVStore<String, T> {
                override fun set(key: String, value: T, expiration: Long) {
                    this@RedisServiceImpl.set(key, value, Duration.ofMillis(expiration)).block()
                }

                override val keys: Set<String>
                    get() = reactiveRedisTemplate
                        .keys("*")
                        .collectList()
                        .block()
                        ?.toSet()
                        ?: emptySet()

                override val size: Int
                    get() = keys.size

                override fun clear() {
                    this@RedisServiceImpl.removeKey(*keys.toTypedArray()).block()
                }

                override fun containsKey(key: String): Boolean {
                    return hasKey(key).block() ?: false
                }

                override fun get(key: String): T? {
                    return this@RedisServiceImpl.get<T>(key).block()
                }

                override fun remove(key: String): T? {
                    val v = get(key)

                    if (v != null) {
                        this@RedisServiceImpl.removeKey(key).block()
                    }

                    return v
                }

                override fun set(key: String, value: T) {
                    this@RedisServiceImpl.set(key, value).block()
                }
            }
        } as ExpiringKVStore<String, T>
    }
}