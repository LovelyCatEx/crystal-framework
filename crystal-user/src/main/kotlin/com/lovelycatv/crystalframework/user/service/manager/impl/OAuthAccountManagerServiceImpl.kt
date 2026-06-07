package com.lovelycatv.crystalframework.user.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.auth.OAuthBindingScope
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.service.manager.OAuthAccountManagerService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class OAuthAccountManagerServiceImpl(
    private val oAuthAccountRepository: OAuthAccountRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : OAuthAccountManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, OAuthAccountEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<OAuthAccountEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<OAuthAccountEntity> = OAuthAccountEntity::class

    override fun getRepository(): OAuthAccountRepository {
        return this.oAuthAccountRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateOAuthAccountDTO): OAuthAccountEntity {
        // Admin-created bindings are system-scoped. One row per (platform, identifier) at SYSTEM scope.
        val existingRows = oAuthAccountRepository
            .findAllByPlatformAndIdentifier(dto.platform, dto.identifier)
            .awaitListWithTimeout()

        existingRows.firstOrNull { it.scope == OAuthBindingScope.SYSTEM.typeId }?.let {
            throw BusinessException("OAuth account '${dto.identifier}' is already linked on platform ${dto.platform}")
        }

        // Cross-row invariant: a third-party identity may only belong to one user.
        if (dto.userId != null) {
            existingRows.firstOrNull { it.userId != null && it.userId != dto.userId }?.let {
                throw BusinessException("OAuth account '${dto.identifier}' already belongs to another user")
            }
        }

        return this.getRepository().save(
            OAuthAccountEntity(
                id = snowIdGenerator.nextId(),
                userId = dto.userId,
                platform = dto.platform,
                identifier = dto.identifier,
                nickname = dto.nickname,
                avatar = dto.avatar,
                scope = OAuthBindingScope.SYSTEM.typeId,
                tenantId = null,
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