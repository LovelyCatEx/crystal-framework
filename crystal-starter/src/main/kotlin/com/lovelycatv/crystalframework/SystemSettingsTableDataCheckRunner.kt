package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.parseObject
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.crystalframework.system.types.SystemSettingsItemValueType
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(1)
@Component
class SystemSettingsTableDataCheckRunner(private val systemSettingsService: SystemSettingsService) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        logger.info("=".repeat(64))
        logger.info("Starting system settings table data checker...")

        val declarations = SystemSettingsConstants.getAllDeclarations()

        logger.info("${declarations.size} system settings declaration(s) detected.")

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
                val declaration = keyWithDeclarationMap[it.configKey]!!

                var testErrorMessage: String? = null
                val testResult = try {
                    when (declaration.valueType) {
                        SystemSettingsItemValueType.STRING -> {
                            // Already is a string
                            copiedConfigValue
                        }
                        SystemSettingsItemValueType.NUMBER -> {
                            copiedConfigValue.toLongOrNull()
                        }
                        SystemSettingsItemValueType.DECIMAL -> {
                            copiedConfigValue.toDoubleOrNull()
                        }
                        SystemSettingsItemValueType.BOOLEAN -> {
                            copiedConfigValue.toBooleanStrictOrNull()
                        }
                        SystemSettingsItemValueType.ENUM_SINGLE -> {
                            if (declaration.enumValues != null) {
                                copiedConfigValue in declaration.enumValues
                            } else {
                                throw IllegalArgumentException("Declaration ${declaration.key} does not have enum values")
                            }
                        }
                        SystemSettingsItemValueType.ENUM_MULTIPLE -> {
                            val parsed = copiedConfigValue.parseObject<List<String>>()
                            if (declaration.enumValues != null) {
                                parsed.all { it in declaration.enumValues }
                            } else {
                                throw IllegalArgumentException("Declaration ${declaration.key} does not have enum values")
                            }
                        }
                    } != null
                } catch (e: Exception) {
                    testErrorMessage = e.localizedMessage ?: e.message ?: "${it.configKey} = ${it.configValue}"
                    false
                }

                if (testResult) {
                    logger.info("  √ ${it.configKey} = $copiedConfigValue")
                    null
                } else {
                    logger.info("  × ${it.configKey} = $copiedConfigValue (expectedType: ${declaration.valueType}, enums: ${declaration.enumValues?.joinToString(" | ")}, message: $testErrorMessage)")
                    it
                }
            } else {
                logger.info("?  ${it.configKey} = null")
                null
            }
        }

        check(!(failedItems.isNotEmpty())) {
            "There were ${failedItems.size} failed system settings item(s). tips: ${SystemSettingsItemValueType.BOOLEAN} only supports \"true\" or \"false\""
        }

        logger.info("=".repeat(64))
    }
}