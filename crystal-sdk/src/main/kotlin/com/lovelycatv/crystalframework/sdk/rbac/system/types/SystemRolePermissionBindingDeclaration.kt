/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.system.types

data class SystemRolePermissionBindingDeclaration(
    val roleName: String,
    val permissionNames: Set<String>,
)
