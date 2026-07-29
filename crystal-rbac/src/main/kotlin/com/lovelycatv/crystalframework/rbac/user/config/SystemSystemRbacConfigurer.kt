package com.lovelycatv.crystalframework.rbac.user.config

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRoleDeclaration
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.constants.SystemRolePermissionRelation
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SystemSystemRbacConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registerPermissions(registry)
        registerRoles(registry)
        registerBindings(registry)
    }

    private fun registerPermissions(registry: SystemRbacRegistry) {
        registry.permissions(SystemPermission.allPermissions())
    }

    private fun registerRoles(registry: SystemRbacRegistry) {
        registry.role(SystemRoleDeclaration(SystemRole.ROLE_ROOT))
        registry.role(SystemRoleDeclaration(SystemRole.ROLE_ADMIN))
        registry.role(SystemRoleDeclaration(SystemRole.ROLE_USER))
    }

    private fun registerBindings(registry: SystemRbacRegistry) {
        registry.grantAll(SystemRole.ROLE_ROOT)

        SystemRolePermissionRelation.mapping
            .filterKeys { it != SystemRole.ROLE_ROOT }
            .forEach { (roleName, permissions) ->
                registry.bind(roleName, permissions.map { it.name })
            }
    }
}
