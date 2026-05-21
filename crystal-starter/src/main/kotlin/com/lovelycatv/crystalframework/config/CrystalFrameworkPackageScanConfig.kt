package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.sdk.config.CrystalFrameworkPackageScanConfigurer
import com.lovelycatv.crystalframework.sdk.config.CrystalFrameworkPackageScanRegistry
import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.persistence.autoconfigure.EntityScanPackages
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.env.Environment
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = [
    "com.lovelycatv.crystalframework"
])
class CrystalFrameworkPackageScanConfig {

    private val logger = logger()

    @Bean
    fun crystalFrameworkPackageScanner(
        environment: Environment,
        packageScanConfigurers: ObjectProvider<CrystalFrameworkPackageScanConfigurer>
    ): BeanDefinitionRegistryPostProcessor {
        val configuredPackages = CrystalFrameworkPackageScanRegistry().apply {
            packageScanConfigurers.orderedStream().forEach { it.configure(this) }
        }

        val componentPackages = mergePackages(
            bindPackages(environment, "crystalframework.scan.base-packages"),
            configuredPackages.componentPackages()
        )
        val entityPackages = mergePackages(
            bindPackages(environment, "crystalframework.scan.entity-packages"),
            configuredPackages.entityPackages()
        )

        return object : BeanDefinitionRegistryPostProcessor, PriorityOrdered {
            override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

            override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
                if (entityPackages.isNotEmpty()) {
                    EntityScanPackages.register(registry, entityPackages)
                    logger.info(
                        "CrystalFramework: registered {} external entity package(s): {}",
                        entityPackages.size,
                        entityPackages
                    )
                } else {
                    logger.debug("CrystalFramework: no external entity packages configured.")
                }

                if (componentPackages.isNotEmpty()) {
                    val scanner = ClassPathBeanDefinitionScanner(registry, true)
                    scanner.setEnvironment(environment)

                    val scannedCount = scanner.scan(*componentPackages.toTypedArray())
                    logger.info(
                        "CrystalFramework: scanned {} external package(s) for Spring components: {}",
                        scannedCount,
                        componentPackages
                    )
                } else {
                    logger.debug("CrystalFramework: no external base packages configured for component scan.")
                }
            }

            override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
                // no-op
            }
        }
    }

    private fun bindPackages(environment: Environment, propertyName: String): List<String> {
        return Binder.get(environment)
            .bind(propertyName, Bindable.listOf(String::class.java))
            .orElse(emptyList())!!
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun mergePackages(first: List<String>, second: List<String>): List<String> {
        return (first + second)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
