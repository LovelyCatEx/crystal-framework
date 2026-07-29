package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.sdk.common.settings.validateConfigValue
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

private const val SECRET_MASK = "***"

@Order(1)
@Component
class SystemSettingsTableDataCheckRunner(
    private val systemSettingsService: SystemSettingsService,
    private val systemSettingsRegistry: SystemSettingsRegistry,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        logger.info("=".repeat(64))
        logger.info("Starting system settings table data checker...")

        val declarations = systemSettingsRegistry.settingDeclarations()

        logger.info("${declarations.size} system settings declaration(s) detected from registry.")

        val allSettings = runBlocking(Dispatchers.IO) {
            systemSettingsService
                .getRepository()
                .findAllByConfigKeyIn(declarations.map { it.key })
                .awaitListWithTimeout()
        }

        val keyWithDeclarationMap = declarations.associateBy { it.key }

        val failedItems = allSettings.mapNotNull {
            val copiedConfigValue = it.configValue
            if (copiedConfigValue != null) {
                val declaration = keyWithDeclarationMap[it.configKey] ?: throw IllegalStateException("System settings declaration not found for key: ${it.configKey}")
                val validation = declaration.validateConfigValue(copiedConfigValue)
                val displayValue = if (declaration.isSecret) SECRET_MASK else copiedConfigValue

                if (validation.pass) {
                    logger.info("  √ ${it.configKey} = $displayValue")
                    null
                } else {
                    logger.info("  × ${it.configKey} = $displayValue (expectedType: ${declaration.valueType}, enums: ${declaration.enumValues?.joinToString(" | ")}, message: ${validation.errorMessage})")
                    it
                }
            } else {
                logger.info("?  ${it.configKey} = null")
                null
            }
        }

        check(failedItems.isEmpty()) {
            "There were ${failedItems.size} failed system settings item(s). tips: ${SettingsItemValueType.BOOLEAN} only supports \"true\" or \"false\""
        }

        logger.info("=".repeat(64))
    }
}