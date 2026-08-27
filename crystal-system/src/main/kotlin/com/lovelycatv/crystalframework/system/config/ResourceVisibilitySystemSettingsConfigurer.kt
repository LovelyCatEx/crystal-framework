package com.lovelycatv.crystalframework.system.config

import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.system.settings.config.SystemSettingsConfigurer
import com.lovelycatv.crystalframework.system.constants.SystemSettingsConstants
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
class ResourceVisibilitySystemSettingsConfigurer(
    private val resourceFileTypeRegistry: ResourceFileTypeRegistry,
) : SystemSettingsConfigurer {
    override fun configure(registry: SystemSettingsRegistry) {
        resourceFileTypeRegistry.declarations().forEach { decl ->
            registry.setting(
                SystemSettingsConstants.Resource.Visibility.declarationFor(
                    fileTypeKey = decl.key,
                    defaultVisibility = decl.defaultVisibility,
                    sort = decl.typeId,
                )
            )
        }
    }
}
