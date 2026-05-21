package com.lovelycatv.crystalframework.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.persistence.autoconfigure.EntityScan
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
    "\${crystalframework.scan.r2dbc-repository-packages:}"
])
class CrystalFrameworkPackageScanConfig {

    private val logger = LoggerFactory.getLogger(CrystalFrameworkPackageScanConfig::class.java)

    @Bean
    fun crystalFrameworkPackageScanner(environment: Environment): BeanDefinitionRegistryPostProcessor {
        val componentPackages = bindPackages(environment, "crystalframework.scan.base-packages")
        val entityPackages = bindPackages(environment, "crystalframework.scan.entity-packages")

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
}
