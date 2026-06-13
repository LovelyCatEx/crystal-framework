package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowEdgeService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowEdgeServiceImpl(
    private val approvalFlowEdgeRepository: ApprovalFlowEdgeRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowEdgeService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowEdgeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowEdgeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowEdgeEntity> = ApprovalFlowEdgeEntity::class

    override fun getRepository(): ApprovalFlowEdgeRepository = approvalFlowEdgeRepository

    override fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowEdgeEntity> {
        return approvalFlowEdgeRepository.findByDefinitionIdAndDefinitionVersion(definitionId, definitionVersion).asFlow()
    }

    override fun findBySourceNodeId(sourceNodeId: Long): Flow<ApprovalFlowEdgeEntity> {
        return approvalFlowEdgeRepository.findBySourceNodeId(sourceNodeId).asFlow()
    }
}
