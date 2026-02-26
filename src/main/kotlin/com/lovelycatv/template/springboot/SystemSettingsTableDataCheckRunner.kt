package com.lovelycatv.template.springboot

import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.template.springboot.system.service.SystemSettingsService
import com.lovelycatv.template.springboot.system.types.SystemSettingsConstants
import com.lovelycatv.template.springboot.system.types.SystemSettingsItemDeclaration
import com.lovelycatv.template.springboot.system.types.SystemSettingsItemValueType
import com.lovelycatv.vertex.log.logger
import jdk.internal.vm.vector.VectorSupport.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

@Order(1)
@Component
class SystemSettingsTableDataCheckRunner(private val systemSettingsService: SystemSettingsService) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        logger.info("=".repeat(64))
        logger.info("Starting system settings table data checker...")

        val declarations = mutableListOf<SystemSettingsItemDeclaration>()
        var nested = SystemSettingsConstants::class.nestedClasses

        while (nested.isNotEmpty()) {
            nested.forEach {
                val instance = it.objectInstance
                it.memberProperties.forEach { prop ->
                    val value = if (instance != null) {
                        prop.call(instance)
                    } else {
                        prop.getter.call()
                    }

                    if (value is SystemSettingsItemDeclaration) {
                        declarations.add(value)
                    }
                }
            }

            nested = nested.flatMap { it.nestedClasses }
        }

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

                val testResult = try {
                    val t = when (declaration.valueType) {
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
                    }

                    if (t != null) {
                        true
                    } else {
                        throw IllegalStateException("")
                    }
                } catch (_: Exception) {
                    false
                }

                if (testResult) {
                    logger.info("  √ ${it.configKey} = $copiedConfigValue")
                    null
                } else {
                    logger.info("  × ${it.configKey} = $copiedConfigValue (expect: ${declaration.valueType})")
                    it
                }
            } else {
                logger.info("?  ${it.configKey} = null")
                null
            }
        }

        if (failedItems.isNotEmpty()) {
            throw IllegalStateException("There were ${failedItems.size} failed system settings item(s). tips: ${SystemSettingsItemValueType.BOOLEAN} only supports \"true\" or \"false\"")
        }

        logger.info("=".repeat(64))
    }
}