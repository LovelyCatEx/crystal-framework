package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.TenantBenefitRegistry
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(4)
@Component
class TenantBenefitTableDataCheckRunner(
    private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    private val tenantBenefitRegistry: TenantBenefitRegistry,
    private val snowIdGenerator: SnowIdGenerator,
    private val systemSettingsService: SystemSettingsService,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val systemSettings = runBlocking(Dispatchers.IO) {
            systemSettingsService.getSystemSettings()
        }

        if (!systemSettings.bootstrap.autoCheckRbacTableData) {
            logger.info(
                "${TenantBenefitTableDataCheckRunner::class.simpleName} is skipped by system settings, " +
                    "if you want to keep the consistency of tenant benefit table data, " +
                    "please set ${SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA.key} to true"
            )
            return
        }

        val benefits = tenantBenefitRegistry.benefitDeclarations()

        logger.info("=".repeat(64))
        logger.info("Total ${benefits.size} tenant benefit(s) detected from registry.")

        logger.info("starting tenant benefit features check...")

        benefits.forEach { declaration ->
            val existing = runBlocking(Dispatchers.IO) {
                benefitFeatureRepository.findByFeatureKey(declaration.featureKey).awaitFirstOrNull()
            }

            if (existing != null) {
                logger.info("  √ ${declaration.featureKey} (name: ${declaration.name})")
            } else {
                runBlocking(Dispatchers.IO) {
                    benefitFeatureRepository.save(
                        TenantTireBenefitFeatureEntity(
                            id = snowIdGenerator.nextId(),
                            featureKey = declaration.featureKey,
                            name = declaration.name,
                            description = declaration.description,
                            featureType = declaration.featureType.typeId,
                            defaultValue = declaration.defaultValue,
                        ).apply { newEntity() }
                    ).awaitFirstOrNull()
                }.also {
                    logger.info("  * ${declaration.featureKey} (name: ${declaration.name})")
                }
            }
        }

        logger.info("=".repeat(64))
    }
}