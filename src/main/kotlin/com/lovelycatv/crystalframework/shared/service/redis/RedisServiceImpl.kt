package com.lovelycatv.crystalframework.shared.service.redis

import org.springframework.data.redis.core.*
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisServiceImpl(
    private val redisTemplate: RedisTemplate<String, Any>
) : RedisService {

    override fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }

    override fun removeKey(vararg key: String): Long {
        return redisTemplate.delete(key.toList())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForValue(): ValueOperations<String, T> {
        return redisTemplate.opsForValue() as ValueOperations<String, T>
    }

    override fun <T : Any> get(key: String): T? {
        return opsForValue<T>().get(key)
    }

    override fun <T : Any> set(
        key: String,
        value: T,
        duration: Duration?
    ) {
        if (duration != null) {
            opsForValue<T>().set(key, value, duration)
        } else {
            opsForValue<T>().set(key, value)
        }
    }

    override fun <T : Any> setIfAbsent(
        key: String,
        value: T,
        duration: Duration?
    ): Boolean {
        return if (duration != null) {
            opsForValue<T>()
                .setIfAbsent(key, value, duration)
        } else {
            opsForValue<T>()
                .setIfAbsent(key, value)
        } ?: false
    }

    override fun <T : Any> setIfPresent(
        key: String,
        value: T,
        duration: Duration?
    ): Boolean {
        return if (duration != null) {
            opsForValue<T>()
                .setIfPresent(key, value, duration)
        } else {
            opsForValue<T>()
                .setIfPresent(key, value)
        } ?: false
    }

    override fun <T : Any> setBit(
        key: String,
        offset: Long,
        value: Boolean
    ): Boolean {
        return opsForValue<T>()
            .setBit(key, offset, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K : Any, V : Any> opsForHash():
            HashOperations<String, K, V> {

        return redisTemplate.opsForHash<K, V>()
                as HashOperations<String, K, V>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForList():
            ListOperations<String, T> {

        return redisTemplate.opsForList()
                as ListOperations<String, T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForSet():
            SetOperations<String, T> {

        return redisTemplate.opsForSet()
                as SetOperations<String, T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForZSet():
            ZSetOperations<String, T> {

        return redisTemplate.opsForZSet()
                as ZSetOperations<String, T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForGeo():
            GeoOperations<String, T> {

        return redisTemplate.opsForGeo()
                as GeoOperations<String, T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> opsForHyperLogLog():
            HyperLogLogOperations<String, T> {

        return redisTemplate.opsForHyperLogLog()
                as HyperLogLogOperations<String, T>
    }
}