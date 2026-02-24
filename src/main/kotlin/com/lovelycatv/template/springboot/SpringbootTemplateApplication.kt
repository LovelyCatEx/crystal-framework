package com.lovelycatv.template.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.rbac.types.PermissionType
import com.lovelycatv.vertex.log.logger
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
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val permissions = SystemPermission::class.memberProperties

        val permissionsByTypes = permissions.groupBy { permissionProperty ->
            PermissionType.entries.find {
                permissionProperty.name.lowercase().startsWith(it.name.lowercase())
            } ?: throw IllegalStateException("Invalid name of permission declaration: ${permissionProperty.name}")
        }

        logger.info("=".repeat(64))
        logger.info("Total ${permissions.size} permission(s) detected.")
        permissionsByTypes.forEach { (type, permissions) ->
            logger.info("${type.name} permission(s):")

            permissions.forEach { permission ->
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
                    logger.info("√ $permissionPropertyName = $permissionKey")
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
                    }

                    logger.info("*  $permissionPropertyName = $permissionKey")
                }
            }
        }
        logger.info("=".repeat(64))
    }
}

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}