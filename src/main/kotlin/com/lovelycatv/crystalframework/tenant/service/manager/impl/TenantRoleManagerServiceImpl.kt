package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.constants.TenantRoleDeclaration
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantRoleManagerServiceImpl(
    private val tenantRoleRepository: TenantRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
) : TenantRoleManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantRoleEntity> = TenantRoleEntity::class

    override fun getRepository(): TenantRoleRepository {
        return tenantRoleRepository
    }

    suspend fun getOrCreate(dto: ManagerCreateTenantRoleDTO): TenantRoleEntity {
        val result = this.getRepository()
            .findByTenantIdAndName(dto.tenantId, dto.name)
            .awaitFirstOrNull()

        return result ?: create(dto)
    }

    override suspend fun create(dto: ManagerCreateTenantRoleDTO): TenantRoleEntity {
        if (this.getRepository().findByTenantIdAndName(dto.tenantId, dto.name).awaitFirstOrNull() != null) {
            throw BusinessException("Tenant role already exists")
        }

        val entity = TenantRoleEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            name = dto.name,
            description = dto.description,
            parentId = dto.parentId
        ).apply { newEntity() }
        return tenantRoleRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant role")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantRoleDTO, original: TenantRoleEntity): TenantRoleEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.parentId?.let { parentId = it }
        }
    }

    override suspend fun createFromDeclaration(
        tenantId: Long,
        declaration: TenantRoleDeclaration
    ): TenantRoleEntity {
        return this.getOrCreate(
            ManagerCreateTenantRoleDTO(
                tenantId = tenantId,
                name = declaration.name,
                description = declaration.description,
                parentId = declaration.parentRole?.let {
                    val parentRoleEntity = getRepository()
                        .findByTenantIdAndName(tenantId, it.name)
                        .awaitFirstOrNull()
                        ?: createFromDeclaration(tenantId, it)

                    parentRoleEntity.id
                }
            )
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        tenantRolePermissionRelationService.deleteByRoleIdIn(ids)

        tenantMemberRoleRelationService.deleteByRoleIdIn(ids)

        // If the role has a parent, change the children's parent to current role's parent
        val rolesWithParent = ids
            .mapNotNull { getByIdOrNull(it) }
            .filter { it.parentId != null }

        rolesWithParent.forEach { roleToBeDeleted ->
            val children = this.getRepository()
                .findByParentId(roleToBeDeleted.id)
                .awaitListWithTimeout()

            children.forEach { childRole ->
                withUpdateEntityContext(childRole) {
                    this.getRepository().save(
                        childRole.apply {
                            this.parentId = roleToBeDeleted.parentId
                        }
                    ).awaitFirstOrNull()
                        ?: throw BusinessException("Could not delete parent role ${roleToBeDeleted.id} " +
                                "because of an exception occurred while updating children roles' parent"
                        )
                }
            }
        }

        super.batchDelete(ids)
    }

    override suspend fun checkIsRelated(ids: Collection<Long>, tenantId: Long): Boolean {
        for (id in ids) {
            if (this.getByIdOrNull(id)?.tenantId != tenantId) {
                return false
            }
        }
        return true
    }
}
