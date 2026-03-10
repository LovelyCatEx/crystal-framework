package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantDepartmentMemberRelationServiceImpl(
    private val tenantDepartmentMemberRelationRepository: TenantDepartmentMemberRelationRepository,
    private val tenantMemberRepository: TenantMemberRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentMemberRelationService {
    override fun getRepository(): TenantDepartmentMemberRelationRepository {
        return tenantDepartmentMemberRelationRepository
    }

    override val cacheStore: ExpiringKVStore<String, TenantDepartmentMemberRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantDepartmentMemberRelationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantDepartmentMemberRelationEntity> = TenantDepartmentMemberRelationEntity::class

    override suspend fun getDepartmentMembers(departmentId: Long): List<TenantMemberEntity> {
        val relationIds = this.getRepository()
            .findAllByDepartmentId(departmentId)
            .awaitListWithTimeout()

        return relationIds.map {
            tenantMemberRepository.findById(it.memberId).awaitFirstOrNull()
                ?: throw BusinessException("member with id ${it.memberId} not found")
        }
    }

    @Transactional
    override suspend fun setDepartmentMembers(departmentId: Long, memberIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findAllByDepartmentId(departmentId)
            .awaitListWithTimeout()

        existing.forEach {
            tenantDepartmentMemberRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        memberIds.forEach { memberId ->
            val entity = TenantDepartmentMemberRelationEntity(
                id = snowIdGenerator.nextId(),
                departmentId = departmentId,
                memberId = memberId,
                roleType = 0 // Default role type
            ).apply { newEntity() }
            tenantDepartmentMemberRelationRepository.save(entity).awaitFirstOrNull()
        }
    }
}
