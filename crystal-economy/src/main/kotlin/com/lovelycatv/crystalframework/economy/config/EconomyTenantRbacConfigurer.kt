/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.config

import com.lovelycatv.crystalframework.economy.constants.EconomyTenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantRole
import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.config.TenantRbacConfigurer
import org.springframework.stereotype.Component

/**
 * Registers economy's tenant-layer permissions and binds them to the tenant super admin (which
 * already holds cross-tenant read-all for approval instances; economy wallet/transaction read
 * follows the same shape).
 */
@Component
class EconomyTenantRbacConfigurer : TenantRbacConfigurer {
    override fun configure(registry: TenantRbacRegistry) {
        registry.permissions(EconomyTenantPermission.allPermissions())
        registry.bind(TenantRole.SUPER_ADMIN.name, EconomyTenantPermission.allPermissions().map { it.name })
    }
}
