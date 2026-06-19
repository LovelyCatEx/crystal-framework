package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTokenRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowTokenService
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowTokenStatus
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowTokenServiceImpl(
    private val approvalFlowTokenRepository: ApprovalFlowTokenRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowTokenService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowTokenEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowTokenEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowTokenEntity> = ApprovalFlowTokenEntity::class

    override fun getRepository(): ApprovalFlowTokenRepository = approvalFlowTokenRepository

    override fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTokenEntity> {
        return approvalFlowTokenRepository.findByInstanceId(instanceId)
    }

    override fun findByInstanceIdAndStatus(instanceId: Long, status: Int): Flow<ApprovalFlowTokenEntity> {
        return approvalFlowTokenRepository.findByInstanceIdAndStatus(instanceId, status)
    }

    override fun findWaitingAtJoin(forkNodeId: Long, joinNodeId: Long): Flow<ApprovalFlowTokenEntity> {
        return approvalFlowTokenRepository.findByForkNodeIdAndCurrentNodeIdAndStatus(
            forkNodeId, joinNodeId, ApprovalFlowTokenStatus.WAITING.typeId
        )
    }
}
