package com.lovelycatv.crystalframework.resource.service.manager.impl

import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerUpdateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderRoutingRuleManagerService
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
class StorageProviderRoutingRuleManagerServiceImpl(
    private val storageProviderRoutingRuleRepository: StorageProviderRoutingRuleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
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
}
