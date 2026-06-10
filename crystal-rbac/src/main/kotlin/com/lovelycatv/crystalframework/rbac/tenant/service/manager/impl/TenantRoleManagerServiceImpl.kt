package com.lovelycatv.crystalframework.rbac.tenant.service.manager.impl

import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.rbac.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantRoleManagerServiceImpl(
    private val tenantRoleRepository: TenantRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantRoleManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantRoleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantRoleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantRoleEntity> = TenantRoleEntity::class

    override fun getRepository(): TenantRoleRepository {
        return tenantRoleRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

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
                parentId = declaration.parentRoleName?.let { parentRoleName ->
                    val parentRoleEntity = getRepository()
                        .findByTenantIdAndName(tenantId, parentRoleName)
                        .awaitFirstOrNull()
                        ?: throw BusinessException("Tenant parent role $parentRoleName not found")

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

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantRoleEntity> {
        return this.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }
}
