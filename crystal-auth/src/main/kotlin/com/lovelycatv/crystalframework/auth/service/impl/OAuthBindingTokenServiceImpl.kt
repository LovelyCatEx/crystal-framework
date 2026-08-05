package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.service.OAuthBindingTokenService
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OAuthBindingTokenServiceImpl(
    private val reactiveRedisService: ReactiveRedisService,
) : OAuthBindingTokenService {
    override suspend fun issue(oauthAccountId: Long): String {
        val token = UUID.randomUUID().toString()
        reactiveRedisService.set(
            key = RedisConstants.OAUTH_BIND_TOKEN_PREFIX + token,
            value = oauthAccountId.toString(),
            duration = RedisConstants.OAUTH_BIND_TOKEN_TTL,
        ).awaitFirstOrNull()
        return token
    }

    override suspend fun consume(token: String): Long {
        val key = RedisConstants.OAUTH_BIND_TOKEN_PREFIX + token
        val value = reactiveRedisService.executeScript(
            "local value = redis.call('get', KEYS[1]); if value then redis.call('del', KEYS[1]); return value else return '' end",
            listOf(key),
            emptyList(),
        ).awaitFirstOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: throw BusinessException("OAuth bind token is invalid or expired")

        return value.trim('"').toLongOrNull()
            ?: throw BusinessException("OAuth bind token is invalid")
    }
}
