package com.lovelycatv.crystalframework.message.service.manager.impl

import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerCreateBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerUpdateBroadcastDTO
import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastRepository
import com.lovelycatv.crystalframework.message.service.manager.BroadcastManagerService
import com.lovelycatv.crystalframework.message.types.BroadcastCategory
import com.lovelycatv.crystalframework.message.utils.toResourceScope
import com.lovelycatv.crystalframework.message.utils.toScopeType
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class BroadcastManagerServiceImpl(
    private val msgBroadcastRepository: MsgBroadcastRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : BroadcastManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, MsgBroadcastEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MsgBroadcastEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MsgBroadcastEntity> = MsgBroadcastEntity::class

    override fun getRepository(): MsgBroadcastRepository = this.msgBroadcastRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateBroadcastDTO): MsgBroadcastEntity {
        val scope = ResourceScope.getById(dto.scope)
            ?: throw BusinessException("broadcast scope ${dto.scope} is invalid")
        val scopeType = scope.toScopeType()
        val audienceType = AudienceType.getByTypeId(dto.audienceType)
            ?: throw BusinessException("broadcast audience type ${dto.audienceType} is invalid")

        validateScopeAudience(scopeType, dto.scopeId, audienceType, dto.audienceRef)

        // The sender party is derived from the scope, never taken from the client: a SYSTEM-scoped
        // broadcast is sent as the system; a TENANT-scoped one is sent as that tenant.
        val storedScopeId = if (scopeType == ScopeType.SYSTEM) null else dto.scopeId
        val senderPartyType = if (scopeType == ScopeType.SYSTEM) PartyType.SYSTEM else PartyType.TENANT
        val senderPartyId = storedScopeId

        return this.getRepository().save(
            MsgBroadcastEntity(
                id = snowIdGenerator.nextId(),
                scopeType = scopeType.typeId,
                scopeId = storedScopeId,
                senderPartyType = senderPartyType.typeId,
                senderPartyId = senderPartyId,
                actingUserId = null,
                category = dto.category ?: BroadcastCategory.ANNOUNCEMENT.typeId,
                audienceType = audienceType.typeId,
                audienceRef = dto.audienceRef,
                title = dto.title,
                content = dto.content,
                publishTime = dto.publishTime ?: System.currentTimeMillis(),
                expireTime = dto.expireTime,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create broadcast")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateBroadcastDTO,
        original: MsgBroadcastEntity
    ): MsgBroadcastEntity {
        return original.apply {
            if (dto.title != null) this.title = dto.title
            if (dto.content != null) this.content = dto.content
            if (dto.category != null) this.category = dto.category
            if (dto.audienceType != null) this.audienceType = dto.audienceType
            if (dto.audienceRef != null) this.audienceRef = dto.audienceRef
            if (dto.publishTime != null) this.publishTime = dto.publishTime
            if (dto.expireTime != null) this.expireTime = dto.expireTime
            // Scope is immutable after creation; re-validate audience against the fixed scope.
            validateScopeAudience(getRealScopeType(), this.scopeId ?: 0, getRealAudienceType(), this.audienceRef)
        }
    }

    override suspend fun resolveRootScope(id: Long): Pair<ResourceScope, Long>? {
        val entity = getByIdOrNull(id) ?: return null
        return entity.getRealScopeType().toResourceScope() to (entity.scopeId ?: 0L)
    }

    /**
     * Enforces scope ↔ audience consistency so a broadcast cannot be projected outside its own
     * scope: a SYSTEM broadcast may only target all users (or a system-level segment), and a
     * TENANT broadcast may only target its own tenant's members ([audienceRef] must equal the
     * tenant's [scopeId]). Both `when`s are exhaustive so a new scope/audience forces a decision
     * here rather than silently slipping through.
     */
    private fun validateScopeAudience(
        scopeType: ScopeType,
        scopeId: Long,
        audienceType: AudienceType,
        audienceRef: Long?,
    ) {
        when (scopeType) {
            ScopeType.SYSTEM -> when (audienceType) {
                AudienceType.ALL_USERS -> {}
                AudienceType.SEGMENT -> {}
                AudienceType.TENANT_MEMBERS ->
                    throw BusinessException("A SYSTEM-scoped broadcast cannot target TENANT_MEMBERS")
            }

            ScopeType.TENANT -> when (audienceType) {
                AudienceType.TENANT_MEMBERS ->
                    if (audienceRef != scopeId) {
                        throw BusinessException("A TENANT-scoped broadcast must target its own tenant (audienceRef must equal scopeId)")
                    }

                AudienceType.ALL_USERS ->
                    throw BusinessException("A TENANT-scoped broadcast cannot target ALL_USERS")

                AudienceType.SEGMENT ->
                    throw BusinessException("A TENANT-scoped SEGMENT broadcast is not supported")
            }
        }
    }
}
