package com.lovelycatv.crystalframework.rbac.config

import com.lovelycatv.crystalframework.sdk.rbac.RbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.config.RbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.types.RbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.types.RbacRoleDeclaration
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.constants.SystemRolePermissionRelation
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberProperties

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SystemRbacConfigurer : RbacConfigurer {
    override fun configure(registry: RbacRegistry) {
        registerPermissions(registry)
        registerRoles(registry)
        registerBindings(registry)
    }

    private fun registerPermissions(registry: RbacRegistry) {
        SystemPermission::class.memberProperties.forEach { property ->
            val permissionKey = property.getter.call() as? String?
                ?: throw IllegalStateException("${property.name} is not a valid permission declaration.")

            val declaration = when {
                permissionKey.contains(":") -> {
                    val (name, path) = permissionKey.split(":", limit = 2)
                    RbacPermissionDeclaration.menu(name = name, path = path)
                }

                permissionKey.contains("@") -> {
                    val (name, path) = permissionKey.split("@", limit = 2)
                    RbacPermissionDeclaration.component(name = name, path = path)
                }

                else -> RbacPermissionDeclaration.action(permissionKey)
            }

            registry.permission(declaration)
        }
    }

    private fun registerRoles(registry: RbacRegistry) {
        registry.role(RbacRoleDeclaration(SystemRole.ROLE_ROOT))
        registry.role(RbacRoleDeclaration(SystemRole.ROLE_ADMIN))
        registry.role(RbacRoleDeclaration(SystemRole.ROLE_USER))
    }

    private fun registerBindings(registry: RbacRegistry) {
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
