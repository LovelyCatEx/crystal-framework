package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.types.LocalFileResourceServiceProperties
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(3)
@Component
class ResourceModuleDatabaseCheckRunner(
    private val storageProviderService: StorageProviderService,
    private val snowIdGenerator: SnowIdGenerator,
    private val systemSettingsService: SystemSettingsService
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val systemSettings = runBlocking(Dispatchers.IO) { systemSettingsService.getSystemSettings() }

        val storageProviders = runBlocking(Dispatchers.IO) {
            storageProviderService
                .getRepository()
                .findAllByActive(true)
                .awaitListWithTimeout()
        }

        if (storageProviders.isEmpty()) {
            logger.warn("No storage providers found in database, creating a default local storage...")
            runBlocking(Dispatchers.IO) {
                storageProviderService
                    .getRepository()
                    .save(
                        StorageProviderEntity(
                            id = snowIdGenerator.nextId(),
                            name = "default",
                            description = "A default local file system storage",
                            type = StorageProviderType.LOCAL_FILE_SYSTEM.typeId,
                            baseUrl = systemSettings.basic.baseUrl,
                            properties = LocalFileResourceServiceProperties(
                                basePath = "./data/uploads/default"
                            ).toJSONString(),
                            active = true
                        ) newEntity true
                    )
                    .awaitFirstOrNull()
                    ?: throw BusinessException("could not create default local storage provider")
            }
        }
    }
}