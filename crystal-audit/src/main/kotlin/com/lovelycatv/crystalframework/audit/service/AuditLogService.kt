/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.types.UserAuthentication

fun interface AuditLogService {
    suspend fun record(
        userAuthentication: UserAuthentication,
        auditRequestInfo: AuditRequestInfo?,
        action: AuditAction,
        resourceType: String,
        resourceIds: List<Long>?,
        success: Boolean,
        errorMessage: String?
    )
}
