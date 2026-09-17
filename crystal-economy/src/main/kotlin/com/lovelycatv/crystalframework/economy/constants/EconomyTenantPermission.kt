/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.constants

import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType

/**
 * Tenant-layer (`i.tenant.`) economy permissions, registered into `TenantRbacRegistry` by
 * [com.lovelycatv.crystalframework.economy.config.EconomyTenantRbacConfigurer].
 */
object EconomyTenantPermission {
    val ACTION_ECONOMY_WALLET_READ = TenantPermissionDeclaration(
        name = "i.tenant.economy.wallet.read",
        description = "Read wallets within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_ECONOMY_TRANSACTION_READ = TenantPermissionDeclaration(
        name = "i.tenant.economy.transaction.read",
        description = "Read economy transactions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> = listOf(
        ACTION_ECONOMY_WALLET_READ,
        ACTION_ECONOMY_TRANSACTION_READ,
    )
}
