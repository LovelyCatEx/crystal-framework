package com.lovelycatv.crystalframework.approval.service.impl

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowDefinitionService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowDefinitionServiceImpl(
    private val approvalFlowDefinitionRepository: ApprovalFlowDefinitionRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher
) : ApprovalFlowDefinitionService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowDefinitionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowDefinitionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowDefinitionEntity> = ApprovalFlowDefinitionEntity::class

    override fun getRepository(): ApprovalFlowDefinitionRepository = approvalFlowDefinitionRepository
}
