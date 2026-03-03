package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.service.OAuthAccountManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class OAuthAccountManagerServiceImpl(
    private val oAuthAccountRepository: OAuthAccountRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService
) : OAuthAccountManagerService {
    override val cacheStore: ExpiringKVStore<String, OAuthAccountEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<OAuthAccountEntity>>
        get() = redisService.asKVStore()
    override fun getRepository(): OAuthAccountRepository {
        return this.oAuthAccountRepository
    }

    override suspend fun create(dto: ManagerCreateOAuthAccountDTO): OAuthAccountEntity {
        return this.getRepository().save(
            OAuthAccountEntity(
                id = snowIdGenerator.nextId(),
                userId = dto.userId,
                platform = dto.platform,
                identifier = dto.identifier,
                nickname = dto.nickname,
                avatar = dto.avatar
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create OAuth account")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateOAuthAccountDTO,
        original: OAuthAccountEntity
    ): OAuthAccountEntity {
        return original.apply {
            this.userId = dto.userId

            if (dto.platform != null) {
                this.platform = dto.platform
            }
            if (dto.identifier != null) {
                this.identifier = dto.identifier
            }
            if (dto.nickname != null) {
                this.nickname = dto.nickname
            }
            if (dto.avatar != null) {
                this.avatar = dto.avatar
            }
        }
    }
}
