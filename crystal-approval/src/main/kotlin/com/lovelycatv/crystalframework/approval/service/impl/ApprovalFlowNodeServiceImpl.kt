package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowNodeService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowNodeServiceImpl(
    private val approvalFlowNodeRepository: ApprovalFlowNodeRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowNodeService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowNodeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowNodeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowNodeEntity> = ApprovalFlowNodeEntity::class

    override fun getRepository(): ApprovalFlowNodeRepository = approvalFlowNodeRepository

    override fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowNodeEntity> {
        return approvalFlowNodeRepository.findByDefinitionIdAndDefinitionVersion(definitionId, definitionVersion).asFlow()
    }
}
