/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.auth

/**
 * Scope at which an OAuth account binding is established.
 *
 * SYSTEM — bound at the platform level (tenant_id is null).
 * TENANT — bound within a specific tenant (tenant_id is set).
 *
 * The same third-party identity may have one SYSTEM binding and at most one binding per tenant,
 * but every non-null binding must belong to the same system user.
 */
enum class OAuthBindingScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);

    companion object {
        fun getByTypeId(typeId: Int): OAuthBindingScope? {
            return entries.find { it.typeId == typeId }
        }
    }
}
