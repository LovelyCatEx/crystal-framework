package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.sdk.common.settings.validateConfigValue
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.settings.entity.TenantSettingsEntity
import com.lovelycatv.crystalframework.tenant.settings.service.TenantSettingsService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(1)
@Component
class TenantSettingsTableDataCheckRunner(
    private val tenantSettingsService: TenantSettingsService,
    private val tenantSettingsRegistry: TenantSettingsRegistry,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        logger.info("=".repeat(64))
        logger.info("Starting tenant settings table data checker...")

        val declarations = tenantSettingsRegistry.settingDeclarations()

        logger.info("${declarations.size} tenant settings declaration(s) detected from registry.")

        val allSettings: List<TenantSettingsEntity> = runBlocking(Dispatchers.IO) {
            tenantSettingsService
                .getRepository()
                .findAll()
                .awaitListWithTimeout()
        }

        if (allSettings.isEmpty()) {
            logger.info("No tenant settings rows yet, skipping value check.")
            logger.info("=".repeat(64))
            return
        }

        val keyWithDeclarationMap = declarations.associateBy { it.key }
        val grouped = allSettings.groupBy { it.tenantId }

        val failedItems = grouped.flatMap { (tenantId, rows) ->
            rows.mapNotNull { row ->
                val copiedConfigValue = row.configValue
                if (copiedConfigValue != null) {
                    val declaration = keyWithDeclarationMap[row.configKey]
                        ?: run {
                            logger.info("?  tenant=$tenantId ${row.configKey} (declaration not found, skipped)")
                            return@mapNotNull null
                        }
                    val validation = declaration.validateConfigValue(copiedConfigValue)

                    if (validation.pass) {
                        logger.info("  √ tenant=$tenantId ${row.configKey} = $copiedConfigValue")
                        null
                    } else {
                        logger.info(
                            "  × tenant=$tenantId ${row.configKey} = $copiedConfigValue " +
                                "(expectedType: ${declaration.valueType}, " +
                                "enums: ${declaration.enumValues?.joinToString(" | ")}, " +
                                "message: ${validation.errorMessage})"
                        )
                        row
                    }
                } else {
                    logger.info("?  tenant=$tenantId ${row.configKey} = null")
                    null
                }
            }
        }

        check(failedItems.isEmpty()) {
            "There were ${failedItems.size} failed tenant settings item(s). " +
                "tips: ${SettingsItemValueType.BOOLEAN} only supports \"true\" or \"false\""
        }

        logger.info("=".repeat(64))
    }
}
