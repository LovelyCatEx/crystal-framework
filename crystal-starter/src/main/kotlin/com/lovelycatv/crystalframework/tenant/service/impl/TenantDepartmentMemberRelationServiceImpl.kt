package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentMemberRelationServiceImpl(
    private val tenantDepartmentMemberRelationRepository: TenantDepartmentMemberRelationRepository,
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

    override suspend fun deleteByMemberIdIn(memberIds: Collection<Long>) {
        this.getRepository()
            .deleteByMemberIdIn(memberIds)
            .awaitFirstOrNull()
    }
}
