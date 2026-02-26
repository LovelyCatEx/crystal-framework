package com.lovelycatv.template.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.constants.SystemRole
import com.lovelycatv.template.springboot.rbac.constants.SystemRolePermissionRelation
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.rbac.service.UserRoleManagerService
import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.rbac.types.PermissionType
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.log.logger
import com.sun.beans.introspect.PropertyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.reflect.full.memberProperties

@SpringBootApplication
class SpringbootTemplateApplication(
    private val userPermissionManagerService: UserPermissionManagerService,
    private val userRoleManagerService: UserRoleManagerService,
    private val userRolePermissionRelationService: UserRolePermissionRelationService,
    private val snowIdGenerator: SnowIdGenerator,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val permissions = SystemPermission::class.memberProperties

        val permissionsByTypes = permissions.groupBy { permissionProperty ->
            PermissionType.entries.find {
                permissionProperty.name.lowercase().startsWith(it.name.lowercase())
            } ?: throw IllegalStateException("Invalid name of permission declaration: ${permissionProperty.name}")
        }

        val roles = SystemRole::class.memberProperties

        logger.info("=".repeat(64))
        logger.info("Total ${roles.size} role(s) and ${permissions.size} permission(s) detected.")

        logger.info("starting permissions check...")

        val permissionsInDatabase = permissionsByTypes.flatMap { (type, permissions) ->
            logger.info("${type.name} permission(s):")

            permissions.map { permission ->
                val permissionPropertyName = permission.name
                val permissionKey = permission.getter.call() as? String?
                    ?: throw IllegalStateException("$permissionPropertyName is not a valid permission declaration.")

                val (name, description, path) = when (type) {
                    PermissionType.ACTION -> {
                        Triple(permissionKey, permissionKey, null)
                    }

                    PermissionType.MENU -> {
                        val (readPermissionKey, path) = permissionKey.split(":")
                        Triple(readPermissionKey, readPermissionKey, path)
                    }
                }

                val existing = runBlocking(Dispatchers.IO) {
                    userPermissionManagerService
                        .getRepository()
                        .findByName(name)
                        .awaitFirstOrNull()
                }

                if (existing != null) {
                    logger.info("  √ $permissionPropertyName = $permissionKey (description: $description, path: $path)")
                    existing
                } else {
                    runBlocking(Dispatchers.IO) {
                        userPermissionManagerService.create(
                            ManagerCreatePermissionDTO(
                                name = name,
                                description = description,
                                type = type.typeId,
                                path = path,
                            )
                        )
                    }.also {
                        logger.info("  * $permissionPropertyName = $permissionKey (description: ${description}, path: $path)")
                    }
                }
            }
        }.associateBy { it.name }

        logger.info("starting roles check...")
        val rolesInDatabase = roles.map { role ->
            val rolePropertyName = role.name
            val roleName = role.getter.call() as? String?
                ?: throw IllegalStateException("$rolePropertyName is not a valid role declaration.")
            val roleDescription = roleName

            val existing = runBlocking(Dispatchers.IO) {
                userRoleManagerService
                    .getRepository()
                    .findByName(roleName)
                    .awaitFirstOrNull()
            }

            if (existing != null) {
                logger.info("  √ $rolePropertyName = $roleName (description: $roleDescription)")
                existing
            } else {
                runBlocking(Dispatchers.IO) {
                    userRoleManagerService.create(
                        ManagerCreateRoleDTO(
                            name = roleName,
                            description = roleDescription
                        )
                    )
                }.also {
                    logger.info("  * $rolePropertyName = $roleName (description: $roleDescription)")
                }
            }
        }.associateBy { it.name }

        logger.info("starting role permission relations check...")
        val roleWithPermissionsMap = SystemRolePermissionRelation.mapping
            .mapKeys { (roleName) ->
                rolesInDatabase[roleName]
                    ?: throw IllegalStateException("Role $roleName is not found in database but declared in ${SystemRolePermissionRelation::class.qualifiedName}")
            }
            .mapValues { (_, permissionNames) ->
                permissionNames.map { permissionName ->
                    val (name) = SystemPermission.resolvePermissionDeclaration(permissionName)
                    permissionsInDatabase[name]
                        ?: throw IllegalStateException("Permission $name is not found in database but declared in ${SystemRolePermissionRelation::class.qualifiedName}")
                }
            }

        roleWithPermissionsMap.forEach { (userRole, userPermissions) ->
            logger.info("${userPermissions.size} permission(s) are related to role ${userRole.name}")

            val permissionsRoleAlreadyHas = runBlocking(Dispatchers.IO) {
                userRolePermissionRelationService
                    .getRolePermissions(userRole.id)
                    .also {
                        it.forEach { permissionEntity ->
                            logger.info("  √ ${permissionEntity.name} (description: ${permissionEntity.description}, path: ${permissionEntity.path})")
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
                                permissionId = absent.id
                            ) newEntity true
                        )
                        .awaitFirstOrNull()
                        ?: throw IllegalStateException("could not save user role permission relation, role: ${userRole.name}(${userRole.id}), permission: ${absent.name}(${absent.id})")
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

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}