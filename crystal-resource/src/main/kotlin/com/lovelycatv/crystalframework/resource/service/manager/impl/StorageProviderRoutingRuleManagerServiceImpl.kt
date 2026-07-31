package com.lovelycatv.crystalframework.resource.service.manager.impl

import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerUpdateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.SimulateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.vo.EvaluationNodeTraceVO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.vo.RuleEvaluationTraceVO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.vo.SimulationResultVO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.vo.StorageProviderSimpleVO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.interfaces.RuleBasedStorageProviderRouter
import com.lovelycatv.crystalframework.resource.interfaces.RoutingContext
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderRoutingRuleManagerService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.RuleEvaluationTrace
import com.lovelycatv.crystalframework.resource.types.SimulationResult
import com.lovelycatv.crystalframework.shared.database.EvaluationGroupTrace
import com.lovelycatv.crystalframework.shared.database.EvaluationLeafTrace
import com.lovelycatv.crystalframework.shared.database.EvaluationNodeTrace
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.KClass

@Service
class StorageProviderRoutingRuleManagerServiceImpl(
    private val storageProviderRoutingRuleRepository: StorageProviderRoutingRuleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val ruleBasedStorageProviderRouter: RuleBasedStorageProviderRouter,
) : StorageProviderRoutingRuleManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, StorageProviderRoutingRuleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<StorageProviderRoutingRuleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<StorageProviderRoutingRuleEntity> = StorageProviderRoutingRuleEntity::class

    override fun getRepository(): StorageProviderRoutingRuleRepository {
        return this.storageProviderRoutingRuleRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateStorageProviderRoutingRuleDTO): StorageProviderRoutingRuleEntity {
        val maxPriority = storageProviderRoutingRuleRepository
            .findAll()
            .awaitListWithTimeout()
            .maxOfOrNull { it.priority } ?: -1

        return this.getRepository().save(
            StorageProviderRoutingRuleEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                priority = maxPriority + 1,
                conditionTree = dto.conditionTree,
                targetProviderIds = dto.targetProviderIds,
                distributionType = dto.distributionType,
                enabled = dto.enabled,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create storage provider routing rule")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateStorageProviderRoutingRuleDTO,
        original: StorageProviderRoutingRuleEntity
    ): StorageProviderRoutingRuleEntity {
        return original.apply {
            if (dto.name != null) {
                this.name = dto.name
            }
            if (dto.conditionTree != null) {
                this.conditionTree = dto.conditionTree
            }
            if (dto.targetProviderIds != null) {
                this.targetProviderIds = dto.targetProviderIds
            }
            if (dto.distributionType != null) {
                this.distributionType = dto.distributionType
            }
            if (dto.enabled != null) {
                this.enabled = dto.enabled
            }
        }
    }

    override suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            withUpdateEntityContext(id) {
                val entity = this.getByIdOrNull(id)
                    ?: throw BusinessException("Storage provider routing rule $id not found")
                entity.priority = index
                this.getRepository().save(entity newEntity false).awaitFirstOrNull()
                    ?: throw BusinessException("Could not reorder storage provider routing rule $id")
            }
        }
    }

    override suspend fun simulate(dto: SimulateStorageProviderRoutingRuleDTO): SimulationResultVO {
        val context = buildRoutingContext(dto)
        val result = ruleBasedStorageProviderRouter.simulate(context)
        return mapToVO(result)
    }

    private fun buildRoutingContext(dto: SimulateStorageProviderRoutingRuleDTO): RoutingContext {
        val fileName = dto.fileName ?: ""
        val uploadTimestamp = dto.uploadTimestamp?.toLongOrNull() ?: System.currentTimeMillis()
        val zoned = Instant.ofEpochMilli(uploadTimestamp).atZone(ZoneId.systemDefault())

        val fileTypeId = dto.fileType
            ?: throw BusinessException("fileType is required for simulation")
        val fileType = ResourceFileType.getByTypeId(fileTypeId)
            ?: throw BusinessException("Unknown fileType id: $fileTypeId")

        return RoutingContext(
            userId = dto.userId?.toLongOrNull() ?: 0L,
            fileType = fileType,
            fileName = fileName,
            fileExtension = dto.fileExtension ?: fileName.substringAfterLast('.', "").lowercase(),
            fileContentType = dto.fileContentType ?: "application/octet-stream",
            fileSize = dto.fileSize?.toLongOrNull() ?: 0L,
            uploadTimestamp = uploadTimestamp,
            hourOfDay = dto.hourOfDay ?: zoned.hour,
            dayOfWeek = dto.dayOfWeek ?: zoned.dayOfWeek.value,
        )
    }

    private fun mapToVO(result: SimulationResult): SimulationResultVO {
        return SimulationResultVO(
            ruleTraces = result.ruleTraces.map { mapTraceToVO(it) },
            finalProvider = result.finalProvider?.let { StorageProviderSimpleVO(id = it.id, name = it.name) },
            noRuleMatched = result.noRuleMatched,
        )
    }

    private fun mapTraceToVO(trace: RuleEvaluationTrace): RuleEvaluationTraceVO {
        return RuleEvaluationTraceVO(
            ruleId = trace.rule.id,
            ruleName = trace.rule.name,
            priority = trace.rule.priority,
            matched = trace.matched,
            conditionTree = trace.conditionTrace?.let { mapNodeToVO(it) },
            selectedProviderIds = trace.selectedProviderIds,
        )
    }

    // "leaf" / "group" are the VO's wire discriminators — see EvaluationNodeTraceVO.type
    private fun mapNodeToVO(node: EvaluationNodeTrace): EvaluationNodeTraceVO {
        return when (node) {
            is EvaluationLeafTrace -> EvaluationNodeTraceVO(
                type = "leaf",
                field = node.field,
                operator = node.operator,
                expectedValue = node.expectedValue,
                actualValue = node.actualValue,
                matched = node.matched,
            )
            is EvaluationGroupTrace -> EvaluationNodeTraceVO(
                type = "group",
                logic = node.logic,
                children = node.children.map { mapNodeToVO(it) },
                matched = node.matched,
            )
        }
    }
}

