package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.service.TenantPermissionService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(4)
@Component
class TenantRbacTableDataCheckRunner(
    private val tenantPermissionService: TenantPermissionService,
    private val tenantPermissionManagerService: TenantPermissionManagerService
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        runBlocking(Dispatchers.IO) {
            check()
        }
    }

    private suspend fun check() {
        logger.info("checking tenant permissions...")
        logger.info("=".repeat(64))

        val permissionDeclarationsMap = TenantPermission
            .allPermissions()
            .groupBy { it.type }

        permissionDeclarationsMap.forEach { (type, permissions) ->
            logger.info("${type.name} permission(s):")

            permissions.forEach {
                val existing = tenantPermissionService
                    .getRepository()
                    .findByName(it.name)
                    .awaitFirstOrNull()
                if (existing != null) {
                    logger.info("  √ ${it.name} (description: ${it.description}, path: ${it.path})")
                } else {
                    tenantPermissionManagerService
                        .create(
                            ManagerCreateTenantPermissionDTO(
                                name = it.name,
                                description = it.description,
                                path = it.path,
                                type = it.type.typeId,
                            )
                        )

                    logger.info("  * ${it.name} (description: ${it.description}, path: ${it.path})")
                }
            }
        }

        logger.info("=".repeat(64))
    }
}