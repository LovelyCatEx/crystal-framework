/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service.result

import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity

data class UserTenantRbacQueryResult(
    val memberId: Long,
    val tenantId: Long,
    val roles: Set<TenantRoleEntity>,
    val permissions: Set<TenantPermissionEntity>,
)