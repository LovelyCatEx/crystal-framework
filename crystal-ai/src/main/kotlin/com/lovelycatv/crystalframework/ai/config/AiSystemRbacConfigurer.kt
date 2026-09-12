package com.lovelycatv.crystalframework.ai.config

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import org.springframework.stereotype.Component

@Component
class AiSystemRbacConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.permissions(AiPermission.allPermissions())
        registry.bind(SystemRole.ROLE_ADMIN, AiPermission.allPermissions().map { it.name })
    }
}
