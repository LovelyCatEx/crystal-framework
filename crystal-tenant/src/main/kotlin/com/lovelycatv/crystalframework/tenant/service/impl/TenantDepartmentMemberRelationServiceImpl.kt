package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentMemberRelationServiceImpl(
    private val tenantDepartmentMemberRelationRepository: TenantDepartmentMemberRelationRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentMemberRelationService {
    override fun getRepository(): TenantDepartmentMemberRelationRepository {
        return tenantDepartmentMemberRelationRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, TenantDepartmentMemberRelationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantDepartmentMemberRelationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantDepartmentMemberRelationEntity> = TenantDepartmentMemberRelationEntity::class

    override suspend fun deleteByMemberIdIn(memberIds: Collection<Long>) {
        this.getRepository()
            .deleteByMemberIdIn(memberIds)
            .awaitFirstOrNull()
    }
}
