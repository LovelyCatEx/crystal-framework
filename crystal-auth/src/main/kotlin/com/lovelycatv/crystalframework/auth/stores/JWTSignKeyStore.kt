package com.lovelycatv.crystalframework.auth.stores

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Holds the JWT signing key in memory so the hot path (every request goes through the auth
 * filter) never touches Redis. The key is warmed once at startup on the boot thread — where
 * blocking is safe — and kept consistent across instances via [setIfAbsent] plus a pub/sub
 * refresh topic. [getSignKey] / [setSignKey] are pure in-memory operations on the request path.
 */
@Component
class JWTSignKeyStore(
    private val reactiveRedisService: ReactiveRedisService,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val reactiveRedisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
) {
    private val logger = logger()

    private val instanceId = UUID.randomUUID().toString()

    private val refreshTopic = ChannelTopic(RedisConstants.JWT_SIGN_KEY_REFRESH_TOPIC)

    @Volatile
    private var cachedSignKey: String? = null

    @PostConstruct
    fun init() {
        // Boot thread: blocking to warm the cache is safe here (not a reactor event-loop).
        runBlocking { loadOrInitSignKey() }

        reactiveRedisMessageListenerContainer
            .receive(refreshTopic)
            .subscribe { message ->
                val sender = message.message
                if (sender == instanceId) {
                    return@subscribe
                }
                logger.info("Received JWT sign key refresh signal from instance $sender")
                cachedSignKey = null
            }
    }

    /**
     * In-memory read. Returns the cached key, lazily (re)loading from Redis only when the cache
     * was invalidated by a refresh signal. The reload bridges with [runBlocking], but this path
     * is hit at most once per refresh, never per request under steady state.
     */
    fun getSignKey(): String {
        return cachedSignKey ?: runBlocking { loadOrInitSignKey() }
    }

    fun setSignKey(signKey: String) {
        runBlocking {
            reactiveRedisService.set(RedisConstants.JWT_SIGN_KEY, signKey).awaitFirstOrNull()
            cachedSignKey = signKey
            reactiveRedisTemplate.convertAndSend(refreshTopic.topic, instanceId).awaitFirstOrNull()
        }
    }

    private suspend fun loadOrInitSignKey(): String {
        val generated = UUID.randomUUID().toString()
        // First instance to start wins; everyone else reads the persisted canonical value.
        reactiveRedisService
            .setIfAbsent(RedisConstants.JWT_SIGN_KEY, generated)
            .awaitFirstOrNull()

        val current = reactiveRedisService
            .get<String>(RedisConstants.JWT_SIGN_KEY)
            .awaitFirstOrNull()
            ?: generated

        cachedSignKey = current
        return current
    }
}
