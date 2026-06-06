package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.user.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(2)
@Component
class RbacTableDataCheckRunner(
    private val userPermissionManagerService: UserPermissionManagerService,
    private val userRoleManagerService: UserRoleManagerService,
    private val userRolePermissionRelationService: UserRolePermissionRelationService,
    private val snowIdGenerator: SnowIdGenerator,
    private val systemSettingsService: SystemSettingsService,
    private val systemRbacRegistry: SystemRbacRegistry,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val systemSettings = runBlocking(Dispatchers.IO) {
            systemSettingsService.getSystemSettings()
        }

        if (!systemSettings.bootstrap.autoCheckRbacTableData) {
            logger.info(
                "${RbacTableDataCheckRunner::class.simpleName} is skipped by system settings, " +
                    "if you want to keep the consistency of RBAC table data, " +
                    "please set ${SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA.key} to true"
            )
            return
        }

        val permissions = systemRbacRegistry.permissionDeclarations()
        val roles = systemRbacRegistry.roleDeclarations()
        val explicitBindings = systemRbacRegistry.rolePermissionBindings().associateBy { it.roleName }
        val grantAllRoles = systemRbacRegistry.grantAllRoleNames()

        logger.info("=".repeat(64))
        logger.info("Total ${roles.size} role(s) and ${permissions.size} permission(s) detected from registry.")

        logger.info("starting permissions check...")

        val permissionsInDatabase = permissions.groupBy { it.type }.flatMap { (type, declarations) ->
            logger.info("${type.name} permission(s):")

            declarations.map { declaration ->
                val existing = runBlocking(Dispatchers.IO) {
                    userPermissionManagerService
                        .getRepository()
                        .findByName(declaration.name)
                        .awaitFirstOrNull()
                }

                if (existing != null) {
                    logger.info(
                        "  √ ${declaration.name} (description: ${declaration.description}, path: ${declaration.path})"
                    )
                    existing
                } else {
                    runBlocking(Dispatchers.IO) {
                        userPermissionManagerService.create(
                            ManagerCreatePermissionDTO(
                                name = declaration.name,
                                description = declaration.description,
                                type = declaration.type.typeId,
                                path = declaration.path,
                            )
                        )
                    }.also {
                        logger.info(
                            "  * ${declaration.name} (description: ${declaration.description}, path: ${declaration.path})"
                        )
                    }
                }
            }
        }.associateBy { it.name }

        logger.info("starting roles check...")
        val rolesInDatabase = roles.map { role ->
            val existing = runBlocking(Dispatchers.IO) {
                userRoleManagerService
                    .getRepository()
                    .findByName(role.name)
                    .awaitFirstOrNull()
            }

            if (existing != null) {
                logger.info("  √ ${role.name} (description: ${role.description})")
                existing
            } else {
                runBlocking(Dispatchers.IO) {
                    userRoleManagerService.create(
                        ManagerCreateRoleDTO(
                            name = role.name,
                            description = role.description,
                        )
                    )
                }.also {
                    logger.info("  * ${role.name} (description: ${role.description})")
                }
            }
        }.associateBy { it.name }

        logger.info("starting role permission relations check...")

        val roleToPermissionNames = linkedMapOf<String, LinkedHashSet<String>>()
        explicitBindings.forEach { (roleName, binding) ->
            roleToPermissionNames.getOrPut(roleName) { linkedSetOf() }.addAll(binding.permissionNames)
        }
        grantAllRoles.forEach { roleName ->
            roleToPermissionNames.getOrPut(roleName) { linkedSetOf() }.addAll(permissions.map { it.name })
        }

        roleToPermissionNames.forEach { (roleName, permissionNames) ->
            val userRole = rolesInDatabase[roleName]
                ?: throw IllegalStateException("Role $roleName is not found in database but declared in registry")

            val userPermissions = permissionNames.map { permissionName ->
                permissionsInDatabase[permissionName]
                    ?: throw IllegalStateException("Permission $permissionName is not found in database but declared in registry")
            }

            logger.info("${userPermissions.size} permission(s) are related to role ${userRole.name}")

            val permissionsRoleAlreadyHas = runBlocking(Dispatchers.IO) {
                userRolePermissionRelationService
                    .getRolePermissions(userRole.id)
                    .also {
                        it.forEach { permissionEntity ->
                            logger.info(
                                "  √ ${permissionEntity.name} (description: ${permissionEntity.description}, path: ${permissionEntity.path})"
                            )
                        }
                    }
                    .map { it.id }
            }

            val absentPermissions = userPermissions.filter {
                it.id !in permissionsRoleAlreadyHas
            }

            if (absentPermissions.isEmpty()) {
                return@forEach
            }

            val saved = runBlocking(Dispatchers.IO) {
                absentPermissions.map { absent ->
                    userRolePermissionRelationService
                        .getRepository()
                        .save(
                            UserRolePermissionRelationEntity(
                                id = snowIdGenerator.nextId(),
                                roleId = userRole.id,
                                permissionId = absent.id,
                            ).apply { newEntity() }
                        )
                        .awaitFirstOrNull()
                        ?: throw IllegalStateException(
                            "could not save user role permission relation, role: ${userRole.name}(${userRole.id}), permission: ${absent.name}(${absent.id})"
                        )
                }
            }.map { it.permissionId }

            absentPermissions.forEach {
                if (it.id !in saved) {
                    logger.warn("  × ${it.name} (description: ${it.description}, path: ${it.path})")
                } else {
                    logger.info("  * ${it.name} (description: ${it.description}, path: ${it.path})")
                }
            }
        }

        logger.info("=".repeat(64))
    }

}
