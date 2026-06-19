package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowRecordDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowRecordDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowRecordRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowRecordManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowRecordManagerServiceImpl(
    private val approvalFlowRecordRepository: ApprovalFlowRecordRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowRecordManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowRecordEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowRecordEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowRecordEntity> = ApprovalFlowRecordEntity::class

    override fun getRepository(): ApprovalFlowRecordRepository = approvalFlowRecordRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowRecordDTO): ApprovalFlowRecordEntity {
        return getRepository().save(
            ApprovalFlowRecordEntity(
                id = snowIdGenerator.nextId(),
                scope = dto.scope,
                scopeId = dto.scopeId,
                instanceId = dto.instanceId,
                nodeId = dto.nodeId,
                operatorId = dto.operatorId,
                action = dto.action,
                comment = dto.comment,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow record")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowRecordDTO,
        original: ApprovalFlowRecordEntity
    ): ApprovalFlowRecordEntity {
        return original.apply {
            if (dto.action != null) this.action = dto.action!!
            if (dto.comment != null) this.comment = dto.comment
        }
    }
}
