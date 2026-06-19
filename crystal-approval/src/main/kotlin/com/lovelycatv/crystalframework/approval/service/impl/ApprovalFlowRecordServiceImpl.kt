package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowRecordRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowRecordService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.flow.Flow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowRecordServiceImpl(
    private val approvalFlowRecordRepository: ApprovalFlowRecordRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowRecordService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowRecordEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowRecordEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowRecordEntity> = ApprovalFlowRecordEntity::class

    override fun getRepository(): ApprovalFlowRecordRepository = approvalFlowRecordRepository

    override fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowRecordEntity> {
        return approvalFlowRecordRepository.findByInstanceId(instanceId)
    }
}
