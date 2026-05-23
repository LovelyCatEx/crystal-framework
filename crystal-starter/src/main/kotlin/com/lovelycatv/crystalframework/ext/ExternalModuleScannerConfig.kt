package com.lovelycatv.crystalframework.ext

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration(proxyBeanMethods = false)
class ExternalModuleScannerConfig {

    @Bean
    fun externalModuleScanner(
        environment: Environment,
    ): ExternalModuleScanner {
        val extDirPath = Binder.get(environment)
            .bind("crystalframework.ext.module-dir", String::class.java)
            .orElse(GlobalConstants.ExtModule.DEFAULT_MODULE_DIR)!!
        return ExternalModuleScanner(extDirPath)
    }
}
