package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowTaskStatus
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowTaskService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowTaskServiceImpl(
    private val approvalFlowTaskRepository: ApprovalFlowTaskRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowTaskService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowTaskEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowTaskEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowTaskEntity> = ApprovalFlowTaskEntity::class

    override fun getRepository(): ApprovalFlowTaskRepository = approvalFlowTaskRepository

    override fun findByInstanceIdAndNodeId(instanceId: Long, nodeId: Long): Flow<ApprovalFlowTaskEntity> {
        return approvalFlowTaskRepository.findByInstanceIdAndNodeId(instanceId, nodeId)
    }

    override fun findPendingByAssigneeId(assigneeId: Long): Flow<ApprovalFlowTaskEntity> {
        return approvalFlowTaskRepository.findByAssigneeIdAndStatus(assigneeId, ApprovalFlowTaskStatus.PENDING.typeId)
    }
}
