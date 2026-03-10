package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberRoleRelationEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRoleRelationRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantMemberRoleRelationServiceImpl(
    private val tenantMemberRoleRelationRepository: TenantMemberRoleRelationRepository,
    private val tenantRoleRepository: TenantRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantMemberRoleRelationService {
    override fun getRepository(): TenantMemberRoleRelationRepository {
        return tenantMemberRoleRelationRepository
    }

    override val cacheStore: ExpiringKVStore<String, TenantMemberRoleRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantMemberRoleRelationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantMemberRoleRelationEntity> = TenantMemberRoleRelationEntity::class

    override suspend fun getMemberRoles(memberId: Long): List<TenantRoleEntity> {
        val relationIds = this.getRepository()
            .findAllByMemberId(memberId)
            .awaitListWithTimeout()

        return relationIds.map {
            tenantRoleRepository.findById(it.roleId).awaitFirstOrNull()
                ?: throw BusinessException("role with id ${it.roleId} not found")
        }
    }

    @Transactional
    override suspend fun setMemberRoles(memberId: Long, roleIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findAllByMemberId(memberId)
            .awaitListWithTimeout()

        existing.forEach {
            tenantMemberRoleRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        roleIds.forEach { roleId ->
            val entity = TenantMemberRoleRelationEntity(
                id = snowIdGenerator.nextId(),
                memberId = memberId,
                roleId = roleId
            ).apply { newEntity() }
            tenantMemberRoleRelationRepository.save(entity).awaitFirstOrNull()
        }
    }
}
