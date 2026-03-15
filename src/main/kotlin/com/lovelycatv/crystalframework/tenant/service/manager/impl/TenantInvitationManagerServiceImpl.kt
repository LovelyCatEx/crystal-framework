package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantInvitationManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantInvitationManagerServiceImpl(
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val redisService: RedisService,
    private val snowIdGenerator: SnowIdGenerator,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantInvitationManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantInvitationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantInvitationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantInvitationEntity> = TenantInvitationEntity::class

    override fun getRepository(): TenantInvitationRepository {
        return this.tenantInvitationRepository
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun create(dto: ManagerCreateInvitationDTO): TenantInvitationEntity {
        val entity = TenantInvitationEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            creatorMemberId = dto.creatorMemberId ?: throw BusinessException("invitation creator id must be specified"),
            departmentId = dto.departmentId,
            invitationCode = generateInvitationCode(dto.tenantId),
            invitationCount = dto.invitationCount,
            expiresTime = dto.expiresTime,
            requiresReviewing = dto.requiresReviewing
        ).apply { newEntity() }

        return tenantInvitationRepository.save(entity).awaitFirstOrNull()
            ?: throw com.lovelycatv.crystalframework.shared.exception.BusinessException("Could not create invitation")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateInvitationDTO,
        original: TenantInvitationEntity
    ): TenantInvitationEntity {
        return original.apply {
            departmentId = dto.departmentId
            dto.invitationCount?.let { invitationCount = it }
            dto.expiresTime?.let { expiresTime = it }
            dto.requiresReviewing?.let { requiresReviewing = it }
            dto.creatorMemberId?.let { creatorMemberId = it }
        }
    }

    override suspend fun checkIsRelated(ids: Collection<Long>, tenantId: Long): Boolean {
        for (id in ids) {
            if (this.getByIdOrNull(id)?.tenantId != tenantId) {
                return false
            }
        }
        return true
    }

    private fun generateInvitationCode(tenantId: Long): String {
        val chars = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-$tenantId"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }
}