package io.github.lovelycatex.playground.config

import com.lovelycatv.crystalframework.sdk.config.CrystalFrameworkPackageScanConfigurer
import io.github.lovelycatex.playground.ExtPlaygroundModule
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@AutoConfiguration
@EnableR2dbcRepositories(basePackageClasses = [ExtPlaygroundModule::class])
class PlaygroundAutoConfiguration {
    @Bean
    fun playgroundPackageScanConfigurer(): CrystalFrameworkPackageScanConfigurer {
        return CrystalFrameworkPackageScanConfigurer { scan ->
            scan.scanBasePackage(ExtPlaygroundModule::class)
            scan.scanEntityPackage(ExtPlaygroundModule::class)
        }
    }
}
