/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

data class TenantRoleDeclaration(
    val name: String,
    val description: String = name,
    val parentRoleName: String? = null,
)
