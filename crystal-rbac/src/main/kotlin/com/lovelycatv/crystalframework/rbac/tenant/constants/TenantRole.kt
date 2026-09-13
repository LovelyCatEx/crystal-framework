/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.constants

import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantRole {
    val MEMBER = TenantRoleDeclaration(
        name = "member"
    )

    val ADMIN = TenantRoleDeclaration(
        name = "admin",
        parentRoleName = MEMBER.name
    )

    val SUPER_ADMIN = TenantRoleDeclaration(
        name = "super_admin",
        parentRoleName = ADMIN.name
    )

    val ROOT = TenantRoleDeclaration(
        name = "root",
        parentRoleName = SUPER_ADMIN.name
    )

    fun allRoles(): List<TenantRoleDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantRole, false)
    }
}