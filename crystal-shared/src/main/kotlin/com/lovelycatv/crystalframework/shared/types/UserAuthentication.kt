/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.shared.types

import com.lovelycatv.crystalframework.shared.exception.BusinessException

data class UserAuthentication(
    val userId: Long,
    val username: String,
    val tenantId: Long?,
) {
    fun assertTenantIdNotNull(): Long {
        return tenantId ?: throw BusinessException("invalid tenant authentication")
    }
}
