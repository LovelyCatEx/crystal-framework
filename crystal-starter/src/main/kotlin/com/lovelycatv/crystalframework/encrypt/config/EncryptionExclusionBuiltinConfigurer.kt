package com.lovelycatv.crystalframework.encrypt.config

import com.lovelycatv.crystalframework.sdk.encrypt.EncryptionExclusionRegistry
import com.lovelycatv.crystalframework.sdk.encrypt.config.EncryptionExclusionConfigurer
import com.lovelycatv.crystalframework.sdk.encrypt.types.EncryptionExclusionDeclaration
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class EncryptionExclusionBuiltinConfigurer : EncryptionExclusionConfigurer {
    override fun configure(registry: EncryptionExclusionRegistry) {
        registry.registers(
            listOf(
                EncryptionExclusionDeclaration(
                    pathPattern = "${GlobalConstants.REQUEST_MAPPING_PREFIX}/actuator/**",
                    description = "Spring Boot Actuator endpoints",
                ),
            )
        )
    }
}
