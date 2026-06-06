package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.config.TenantRbacConfigurer
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantRole
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantRolePermissionRelation
import org.springframework.stereotype.Component

@Component
class TenantBuiltinRbacConfigurer : TenantRbacConfigurer {
    override fun configure(registry: TenantRbacRegistry) {
        registry.permissions(TenantPermission.allPermissions())
        registry.roles(TenantRole.allRoles())

        TenantRolePermissionRelation.mapping.forEach { (role, permissions) ->
            registry.bind(
                role.name,
                permissions.map { it.name }
            )
        }

        registry.defaultOwnerRole(TenantRole.ROOT.name)
        registry.defaultMemberRole(TenantRole.MEMBER.name)
    }
}
