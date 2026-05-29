package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(3)
@Component
class TenantRbacTableDataCheckRunner(
    private val tenantPermissionManagerService: TenantPermissionManagerService,
    private val tenantRbacRegistry: TenantRbacRegistry,
    private val systemSettingsService: SystemSettingsService,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val systemSettings = runBlocking(Dispatchers.IO) {
            systemSettingsService.getSystemSettings()
        }

        if (!systemSettings.bootstrap.autoCheckRbacTableData) {
            logger.info(
                "${TenantRbacTableDataCheckRunner::class.simpleName} is skipped by system settings, " +
                    "if you want to keep the consistency of tenant RBAC table data, " +
                    "please set ${SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA.key} to true"
            )
            return
        }

        val permissions = tenantRbacRegistry.permissionDeclarations()

        logger.info("=".repeat(64))
        logger.info("Total ${permissions.size} tenant permission(s) detected from registry.")

        logger.info("starting tenant permissions check...")

        permissions.forEach { declaration ->
            val existing = runBlocking(Dispatchers.IO) {
                tenantPermissionManagerService
                    .getRepository()
                    .findByName(declaration.name)
                    .awaitFirstOrNull()
            }

            if (existing != null) {
                logger.info("  √ ${declaration.name} (description: ${declaration.description}, path: ${declaration.path})")
            } else {
                runBlocking(Dispatchers.IO) {
                    tenantPermissionManagerService.create(
                        ManagerCreateTenantPermissionDTO(
                            name = declaration.name,
                            description = declaration.description,
                            type = declaration.type.typeId,
                            path = declaration.path,
                        )
                    )
                }.also {
                    logger.info("  * ${declaration.name} (description: ${declaration.description}, path: ${declaration.path})")
                }
            }
        }

        logger.info("=".repeat(64))
    }

}