package com.lovelycatv.crystalframework.rbac.user.config

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRoleDeclaration
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.constants.SystemRolePermissionRelation
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SystemSystemRbacConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registerPermissions(registry)
        registerRoles(registry)
        registerBindings(registry)
    }

    private fun registerPermissions(registry: SystemRbacRegistry) {
        SystemPermission::class.memberProperties
            .filter { it.returnType.classifier == String::class }
            .forEach { property ->
                val permissionKey = property.getter.call() as? String?
                    ?: throw IllegalStateException("${property.name} is not a valid permission declaration.")

                val declaration = when {
                    permissionKey.contains(":") -> {
                        val (name, path) = permissionKey.split(":", limit = 2)
                        SystemRbacPermissionDeclaration.menu(name = name, path = path)
                    }

                    permissionKey.contains("@") -> {
                        val (name, path) = permissionKey.split("@", limit = 2)
                        SystemRbacPermissionDeclaration.component(name = name, path = path)
                    }

                    else -> SystemRbacPermissionDeclaration.action(permissionKey)
                }

                val enriched = declaration.copy(
                    description = SystemPermission.DESCRIPTIONS[declaration.name] ?: declaration.description
                )

                registry.permission(enriched)
            }
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
            .forEach { (roleName, permissionNames) ->
                registry.bind(roleName, permissionNames.map { normalizePermissionName(it) })
            }
    }

    private fun normalizePermissionName(permissionKey: String): String {
        return when {
            permissionKey.contains(":") -> permissionKey.substringBefore(":")
            permissionKey.contains("@") -> permissionKey.substringBefore("@")
            else -> permissionKey
        }
    }
}
