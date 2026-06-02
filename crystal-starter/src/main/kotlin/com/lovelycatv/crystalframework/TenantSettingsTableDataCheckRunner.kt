package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.parseObject
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

                    var testErrorMessage: String? = null
                    val testResult = try {
                        when (declaration.valueType) {
                            SystemSettingsItemValueType.STRING -> copiedConfigValue
                            SystemSettingsItemValueType.NUMBER -> copiedConfigValue.toLongOrNull()
                            SystemSettingsItemValueType.DECIMAL -> copiedConfigValue.toDoubleOrNull()
                            SystemSettingsItemValueType.BOOLEAN -> copiedConfigValue.toBooleanStrictOrNull()
                            SystemSettingsItemValueType.ENUM_SINGLE -> {
                                val enumValues = declaration.enumValues
                                    ?: throw IllegalArgumentException("Declaration ${declaration.key} does not have enum values")
                                copiedConfigValue in enumValues
                            }
                            SystemSettingsItemValueType.ENUM_MULTIPLE -> {
                                val enumValues = declaration.enumValues
                                    ?: throw IllegalArgumentException("Declaration ${declaration.key} does not have enum values")
                                val parsed = copiedConfigValue.parseObject<List<String>>()
                                parsed.all { it in enumValues }
                            }
                        } != null
                    } catch (e: Exception) {
                        testErrorMessage = e.localizedMessage ?: e.message ?: "${row.configKey} = ${row.configValue}"
                        false
                    }

                    if (testResult) {
                        logger.info("  √ tenant=$tenantId ${row.configKey} = $copiedConfigValue")
                        null
                    } else {
                        logger.info(
                            "  × tenant=$tenantId ${row.configKey} = $copiedConfigValue " +
                                "(expectedType: ${declaration.valueType}, " +
                                "enums: ${declaration.enumValues?.joinToString(" | ")}, " +
                                "message: $testErrorMessage)"
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
                "tips: ${SystemSettingsItemValueType.BOOLEAN} only supports \"true\" or \"false\""
        }

        logger.info("=".repeat(64))
    }
}
