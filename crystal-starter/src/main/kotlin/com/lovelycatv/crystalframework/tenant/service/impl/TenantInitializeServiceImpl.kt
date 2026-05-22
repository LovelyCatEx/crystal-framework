package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.sdk.tenant.rbac.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.tenant.rbac.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.tenant.rbac.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.service.TenantInitializeService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.tenant.types.TenantSettings
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TenantInitializeServiceImpl(
    private val tenantRoleManagerService: TenantRoleManagerService,
    private val tenantPermissionManagerService: TenantPermissionManagerService,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantMemberManagerService: TenantMemberManagerService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantService: TenantService,
    private val tenantRbacRegistry: TenantRbacRegistry,
) : TenantInitializeService {
    private val logger = logger()

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun initializeTenant(tenantId: Long, ownerUserId: Long) {
        val tenant = tenantService.getByIdOrNull(tenantId)
            ?: throw BusinessException("Tenant not found")

        logger.info("Initializing Tenant: ${tenant.name}, details: ${tenant.toJSONString()}")

        val permissionEntityMap = ensurePermissions(tenantRbacRegistry.permissionDeclarations())
        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: permissions initialized")

        val roleEntityMap = sortRolesByParent(tenantRbacRegistry.roleDeclarations())
            .associate { role ->
                role.name to tenantRoleManagerService.createFromDeclaration(tenant.id, role)
            }
        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: roles initialized")

        tenantRbacRegistry.rolePermissionBindings().forEach { binding ->
            val roleEntity = roleEntityMap[binding.roleName]
                ?: throw BusinessException("Tenant role ${binding.roleName} not found when binding permissions")

            val permissionIds = binding.permissionNames.map { permissionName ->
                permissionEntityMap[permissionName]?.id
                    ?: throw BusinessException("Tenant permission $permissionName not found when binding role ${binding.roleName}")
            }

            tenantRolePermissionRelationService.setRolePermissions(
                roleId = roleEntity.id,
                permissionIds = permissionIds
            )
        }
        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: role permission bindings initialized")

        val defaultOwnerRoleName = tenantRbacRegistry.defaultOwnerRoleName()
            ?: throw BusinessException("Tenant default owner role is not configured")
        val defaultMemberRoleName = tenantRbacRegistry.defaultMemberRoleName()
            ?: throw BusinessException("Tenant default member role is not configured")

        val settings = TenantSettings(
            defaultOwnerRoleId = roleEntityMap[defaultOwnerRoleName]?.id
                ?: throw BusinessException("Tenant default owner role $defaultOwnerRoleName not found"),
            defaultMemberRoleId = roleEntityMap[defaultMemberRoleName]?.id
                ?: throw BusinessException("Tenant default member role $defaultMemberRoleName not found"),
        )

        tenantService.withUpdateEntityContext(tenant) {
            tenantService.getRepository().save(
                tenant.apply {
                    this.settings = settings.toJSONString()
                }
            ).awaitFirstOrNull() ?: throw BusinessException("Could not save tenant settings when initializing")
        }

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: settings initialized, data: ${tenant.settings}")

        val ownerMember = tenantMemberManagerService.create(
            ManagerCreateTenantMemberDTO(
                tenantId = tenant.id,
                memberUserId = ownerUserId,
                status = TenantMemberStatus.ACTIVE.typeId,
            )
        )

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: owner member relation initialized, data: ${tenant.settings}")

        tenantMemberRoleRelationService.setMemberRoles(
            memberId = ownerMember.id,
            roleIds = listOf(settings.defaultOwnerRoleId)
        )

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: owner member role initialized, roleId: ${settings.defaultOwnerRoleId}")
    }

    private suspend fun ensurePermissions(
        declarations: List<TenantPermissionDeclaration>
    ) = declarations.associate { declaration ->
        val permissionName = declaration.name.trim()
        if (permissionName.isBlank()) {
            throw BusinessException("Tenant permission name must not be blank")
        }

        val entity = tenantPermissionManagerService
            .getRepository()
            .findByName(permissionName)
            .awaitFirstOrNull()
            ?: tenantPermissionManagerService.create(
                ManagerCreateTenantPermissionDTO(
                    name = permissionName,
                    description = declaration.description,
                    path = declaration.path,
                    type = declaration.type.typeId,
                )
            )

        permissionName to entity
    }

    private fun sortRolesByParent(declarations: List<TenantRoleDeclaration>): List<TenantRoleDeclaration> {
        val rolesByName = declarations
            .map { role ->
                role.copy(
                    name = role.name.trim(),
                    parentRoleName = role.parentRoleName?.trim()?.takeIf { it.isNotBlank() }
                )
            }
            .filter { it.name.isNotBlank() }
            .associateBy { it.name }

        val visitingState = mutableMapOf<String, Int>()
        val sorted = mutableListOf<TenantRoleDeclaration>()

        fun visit(role: TenantRoleDeclaration) {
            when (visitingState[role.name]) {
                1 -> throw BusinessException("Circular tenant role parent dependency detected at ${role.name}")
                2 -> return
            }

            visitingState[role.name] = 1

            role.parentRoleName?.let { parentRoleName ->
                val parentRole = rolesByName[parentRoleName]
                    ?: throw BusinessException("Tenant parent role $parentRoleName for role ${role.name} is not declared")
                visit(parentRole)
            }

            visitingState[role.name] = 2
            sorted.add(role)
        }

        rolesByName.values.forEach { visit(it) }
        return sorted
    }
}
