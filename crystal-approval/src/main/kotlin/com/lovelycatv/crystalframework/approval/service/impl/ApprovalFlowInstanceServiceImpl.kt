package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowInstanceService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowInstanceServiceImpl(
    private val approvalFlowInstanceRepository: ApprovalFlowInstanceRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowInstanceService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowInstanceEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowInstanceEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowInstanceEntity> = ApprovalFlowInstanceEntity::class

    override fun getRepository(): ApprovalFlowInstanceRepository = approvalFlowInstanceRepository

    override fun findByScopeAndScopeId(scope: Int, scopeId: Long): Flow<ApprovalFlowInstanceEntity> {
        return approvalFlowInstanceRepository.findByScopeAndScopeId(scope, scopeId)
    }

    override fun findByInitiatorId(initiatorId: Long): Flow<ApprovalFlowInstanceEntity> {
        return approvalFlowInstanceRepository.findByInitiatorId(initiatorId)
    }
}
