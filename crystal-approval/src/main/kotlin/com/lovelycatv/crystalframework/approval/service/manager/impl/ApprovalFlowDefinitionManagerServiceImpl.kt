package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
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
class ApprovalFlowDefinitionManagerServiceImpl(
    private val approvalFlowDefinitionRepository: ApprovalFlowDefinitionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowDefinitionManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowDefinitionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowDefinitionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowDefinitionEntity> = ApprovalFlowDefinitionEntity::class

    override fun getRepository(): ApprovalFlowDefinitionRepository = approvalFlowDefinitionRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowDefinitionDTO): ApprovalFlowDefinitionEntity {
        return getRepository().save(
            ApprovalFlowDefinitionEntity(
                id = snowIdGenerator.nextId(),
                scope = dto.scope,
                scopeId = dto.scopeId,
                name = dto.name,
                description = dto.description,
                formSchema = dto.formSchema,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow definition")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowDefinitionDTO,
        original: ApprovalFlowDefinitionEntity
    ): ApprovalFlowDefinitionEntity {
        return original.apply {
            if (dto.name != null) this.name = dto.name!!
            if (dto.description != null) this.description = dto.description
            if (dto.status != null) this.status = dto.status!!
            if (dto.formSchema != null) this.formSchema = dto.formSchema
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<ApprovalFlowDefinitionEntity> {
        return approvalFlowDefinitionRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }
}
