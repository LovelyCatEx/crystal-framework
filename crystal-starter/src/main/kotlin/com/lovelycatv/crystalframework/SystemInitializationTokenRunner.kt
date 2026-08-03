package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.service.SystemInitializeService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(5)
@Component
class SystemInitializationTokenRunner(
    private val systemInitializeService: SystemInitializeService,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val token = try {
            runBlocking(Dispatchers.IO) {
                systemInitializeService.prepareInitializationToken()
            }
        } catch (e: BusinessException) {
            logger.error("System initialization token preparation error: ${e.message}")
            return
        }

        if (token == null) {
            logger.info("System initialization token already exists or the system has been initialized.")
            return
        }

        logger.warn("=".repeat(64))
        logger.warn("One-time system initialization token: $token")
        logger.warn("Enter this token on the system initialization page. It will be invalidated after successful initialization.")
        logger.warn("=".repeat(64))
    }
}
