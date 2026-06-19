package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowTaskManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowTaskManagerServiceImpl(
    private val approvalFlowTaskRepository: ApprovalFlowTaskRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowTaskManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowTaskEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowTaskEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowTaskEntity> = ApprovalFlowTaskEntity::class

    override fun getRepository(): ApprovalFlowTaskRepository = approvalFlowTaskRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowTaskDTO): ApprovalFlowTaskEntity {
        return getRepository().save(
            ApprovalFlowTaskEntity(
                id = snowIdGenerator.nextId(),
                scope = dto.scope,
                scopeId = dto.scopeId,
                instanceId = dto.instanceId,
                nodeId = dto.nodeId,
                assigneeId = dto.assigneeId,
                formData = dto.formData,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow task")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowTaskDTO,
        original: ApprovalFlowTaskEntity
    ): ApprovalFlowTaskEntity {
        return original.apply {
            if (dto.status != null) this.status = dto.status!!
            if (dto.comment != null) this.comment = dto.comment
            if (dto.formData != null) this.formData = dto.formData
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<ApprovalFlowTaskEntity> {
        return approvalFlowTaskRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }
}
