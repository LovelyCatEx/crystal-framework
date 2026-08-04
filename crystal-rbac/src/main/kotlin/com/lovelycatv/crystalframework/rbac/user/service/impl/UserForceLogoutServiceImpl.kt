package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.service.UserForceLogoutService
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserForceLogoutServiceImpl(
    private val redisService: ReactiveRedisService,
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) : UserForceLogoutService {
    private fun redisKey(userId: Long): String = RedisConstants.getForceLogoutKey(userId)

    override suspend fun markForceLogout(userId: Long) {
        redisService.set(
            redisKey(userId),
            System.currentTimeMillis().toString(),
            crystalFrameworkConfiguration.auth.jwt.expiration
        ).awaitFirstOrNull()
    }

    override suspend fun getForceLogoutAt(userId: Long): Long? {
        return redisService
            .get<String>(redisKey(userId))
            .awaitFirstOrNull()
            ?.toLongOrNull()
    }
}
