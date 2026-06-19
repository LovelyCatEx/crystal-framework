package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo.TenantDictItemTreeVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictItemRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictItemManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDictItemManagerServiceImpl(
    private val tenantDictItemRepository: TenantDictItemRepository,
    private val tenantDictTypeManagerService: TenantDictTypeManagerService,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantDictItemManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantDictItemEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantDictItemEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantDictItemEntity> = TenantDictItemEntity::class

    override fun getRepository(): TenantDictItemRepository = tenantDictItemRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantDictItemDTO): TenantDictItemEntity {
        val entity = TenantDictItemEntity(
            id = snowIdGenerator.nextId(),
            typeId = dto.typeId,
            itemCode = dto.itemCode,
            itemValue = dto.itemValue,
            parentId = dto.parentId,
            sortOrder = dto.sortOrder ?: 0,
            isDefault = dto.isDefault ?: false,
            status = dto.status ?: 1
        ).apply { newEntity() }
        return tenantDictItemRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant dict item")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateTenantDictItemDTO,
        original: TenantDictItemEntity
    ): TenantDictItemEntity {
        return original.apply {
            dto.itemValue?.let { itemValue = it }
            dto.parentId?.let { parentId = it }
            dto.sortOrder?.let { sortOrder = it }
            dto.isDefault?.let { isDefault = it }
            dto.status?.let { status = it }
        }
    }

    override suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO> {
        val allItems = tenantDictItemRepository.findAllByTypeId(typeId).awaitListWithTimeout()
        return buildTree(allItems, null)
    }

    private fun buildTree(allItems: List<TenantDictItemEntity>, parentId: Long?): List<TenantDictItemTreeVO> {
        return allItems
            .filter { it.parentId == parentId }
            .sortedBy { it.sortOrder }
            .map { item ->
                TenantDictItemTreeVO(
                    id = item.id.toString(),
                    itemCode = item.itemCode,
                    itemValue = item.itemValue,
                    parentId = item.parentId?.toString(),
                    sortOrder = item.sortOrder,
                    isDefault = item.isDefault,
                    status = item.status,
                    createdTime = item.createdTime.toString(),
                    modifiedTime = item.modifiedTime.toString(),
                    children = buildTree(allItems, item.id)
                )
            }
    }

    override suspend fun checkIsRelatedToRootParent(ids: Collection<Long>, rootParentId: Long): Boolean {
        for (id in ids) {
            val item = this.getByIdOrNull(id) ?: return false
            if (!tenantDictTypeManagerService.checkIsRelatedToRootParent(item.typeId, rootParentId)) {
                return false
            }
        }
        return true
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantDictItemEntity> {
        val typeIds = tenantDictTypeManagerService.findAllByScopeId(tenantId).map { it.id }
        if (typeIds.isEmpty()) return emptyList()
        return typeIds.flatMap { typeId ->
            tenantDictItemRepository.findAllByTypeId(typeId).awaitListWithTimeout()
        }
    }
}
