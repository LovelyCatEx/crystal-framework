/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.constants

import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemRbacPermissionDeclaration

/**
 * System-layer (super + system) economy permissions, registered into `SystemRbacRegistry` by
 * [com.lovelycatv.crystalframework.economy.config.EconomySystemRbacConfigurer]. Tenant-layer
 * (`i.tenant.`) permissions live in [EconomyTenantPermission] and are registered by the tenant
 * configurer.
 */
object EconomyPermission {
    const val ACTION_SYSTEM_ECONOMY_CURRENCY_CREATE_NAME = "system.economy.currency.create"
    val ACTION_SYSTEM_ECONOMY_CURRENCY_CREATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_ECONOMY_CURRENCY_CREATE_NAME, "Create currencies")

    const val ACTION_SYSTEM_ECONOMY_CURRENCY_READ_NAME = "system.economy.currency.read"
    val ACTION_SYSTEM_ECONOMY_CURRENCY_READ = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_ECONOMY_CURRENCY_READ_NAME, "Read currencies")

    const val ACTION_SYSTEM_ECONOMY_CURRENCY_UPDATE_NAME = "system.economy.currency.update"
    val ACTION_SYSTEM_ECONOMY_CURRENCY_UPDATE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_ECONOMY_CURRENCY_UPDATE_NAME, "Update currencies")

    const val ACTION_SYSTEM_ECONOMY_CURRENCY_DELETE_NAME = "system.economy.currency.delete"
    val ACTION_SYSTEM_ECONOMY_CURRENCY_DELETE = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_ECONOMY_CURRENCY_DELETE_NAME, "Delete currencies")

    val ACTION_X_ECONOMY_WALLET_READ = SystemRbacPermissionDeclaration.action("x.economy.wallet.read", "Read wallets in any scope")
    val ACTION_SYSTEM_ECONOMY_WALLET_READ = SystemRbacPermissionDeclaration.action("system.economy.wallet.read", "Read system-scope wallets")
    val ACTION_TENANT_ECONOMY_WALLET_READ = SystemRbacPermissionDeclaration.action("tenant.economy.wallet.read", "Read tenant-scope wallets across tenants")

    const val ACTION_SYSTEM_ECONOMY_WALLET_ADJUST_NAME = "system.economy.wallet.adjust"
    val ACTION_SYSTEM_ECONOMY_WALLET_ADJUST = SystemRbacPermissionDeclaration.action(ACTION_SYSTEM_ECONOMY_WALLET_ADJUST_NAME, "Adjust wallet balances")

    val ACTION_X_ECONOMY_TRANSACTION_READ = SystemRbacPermissionDeclaration.action("x.economy.transaction.read", "Read economy transactions in any scope")
    val ACTION_SYSTEM_ECONOMY_TRANSACTION_READ = SystemRbacPermissionDeclaration.action("system.economy.transaction.read", "Read system-scope economy transactions")
    val ACTION_TENANT_ECONOMY_TRANSACTION_READ = SystemRbacPermissionDeclaration.action("tenant.economy.transaction.read", "Read tenant-scope economy transactions across tenants")

    val MENU_SYSTEM_ECONOMY_CURRENCY = SystemRbacPermissionDeclaration.menu("system.economy.currency", "/manager/economy/currency", "Currency management menu")
    val MENU_SYSTEM_ECONOMY_WALLET = SystemRbacPermissionDeclaration.menu("system.economy.wallet", "/manager/economy/wallet", "Wallet management menu")
    val MENU_TENANT_ECONOMY_WALLET = SystemRbacPermissionDeclaration.menu("tenant.economy.wallet", "/manager/tenant/wallet", "Tenant wallet management menu")
    val MENU_SYSTEM_ECONOMY_TRANSACTION = SystemRbacPermissionDeclaration.menu("system.economy.transaction", "/manager/economy/transaction", "Economy transaction menu")
    val MENU_TENANT_ECONOMY_TRANSACTION = SystemRbacPermissionDeclaration.menu("tenant.economy.transaction", "/manager/tenant/transaction", "Tenant economy transaction menu")

    fun allPermissions(): List<SystemRbacPermissionDeclaration> = listOf(
        ACTION_SYSTEM_ECONOMY_CURRENCY_CREATE,
        ACTION_SYSTEM_ECONOMY_CURRENCY_READ,
        ACTION_SYSTEM_ECONOMY_CURRENCY_UPDATE,
        ACTION_SYSTEM_ECONOMY_CURRENCY_DELETE,
        ACTION_X_ECONOMY_WALLET_READ,
        ACTION_SYSTEM_ECONOMY_WALLET_READ,
        ACTION_TENANT_ECONOMY_WALLET_READ,
        ACTION_SYSTEM_ECONOMY_WALLET_ADJUST,
        ACTION_X_ECONOMY_TRANSACTION_READ,
        ACTION_SYSTEM_ECONOMY_TRANSACTION_READ,
        ACTION_TENANT_ECONOMY_TRANSACTION_READ,
        MENU_SYSTEM_ECONOMY_CURRENCY,
        MENU_SYSTEM_ECONOMY_WALLET,
        MENU_TENANT_ECONOMY_WALLET,
        MENU_SYSTEM_ECONOMY_TRANSACTION,
        MENU_TENANT_ECONOMY_TRANSACTION,
    )
}
