/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types

import com.lovelycatv.crystalframework.shared.exception.BusinessException

data class UserAuthentication(
    val userId: Long,
    val username: String,
    val tenantId: Long?,
    val tenantMemberId: Long?,
) {
    fun assertTenantIdNotNull(): Long {
        return tenantId ?: throw BusinessException("invalid tenant authentication")
    }
}
