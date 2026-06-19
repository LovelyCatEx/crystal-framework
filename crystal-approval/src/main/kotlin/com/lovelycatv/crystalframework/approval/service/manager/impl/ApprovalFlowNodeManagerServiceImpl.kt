package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowNodeDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowNodeDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowNodeManagerService
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
class ApprovalFlowNodeManagerServiceImpl(
    private val approvalFlowNodeRepository: ApprovalFlowNodeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowNodeManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowNodeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowNodeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowNodeEntity> = ApprovalFlowNodeEntity::class

    override fun getRepository(): ApprovalFlowNodeRepository = approvalFlowNodeRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowNodeDTO): ApprovalFlowNodeEntity {
        return getRepository().save(
            ApprovalFlowNodeEntity(
                id = snowIdGenerator.nextId(),
                definitionId = dto.definitionId,
                definitionVersion = dto.definitionVersion,
                nodeKey = dto.nodeKey,
                type = dto.type,
                name = dto.name,
                config = dto.config,
                formSchema = dto.formSchema,
                positionX = dto.positionX,
                positionY = dto.positionY,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow node")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowNodeDTO,
        original: ApprovalFlowNodeEntity
    ): ApprovalFlowNodeEntity {
        return original.apply {
            if (dto.nodeKey != null) this.nodeKey = dto.nodeKey!!
            if (dto.type != null) this.type = dto.type!!
            if (dto.name != null) this.name = dto.name!!
            if (dto.config != null) this.config = dto.config
            if (dto.formSchema != null) this.formSchema = dto.formSchema
            if (dto.positionX != null) this.positionX = dto.positionX!!
            if (dto.positionY != null) this.positionY = dto.positionY!!
        }
    }

    override suspend fun getNodesByDefinitionsIdAndVersion(
        id: Long,
        currentVersion: Int
    ): List<ApprovalFlowNodeEntity> {
        return getRepository()
            .findByDefinitionIdAndDefinitionVersion(id, currentVersion)
            .awaitListWithTimeout()
    }
}
