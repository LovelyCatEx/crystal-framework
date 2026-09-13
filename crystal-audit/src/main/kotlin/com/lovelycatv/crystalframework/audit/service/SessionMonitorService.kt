/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.types.SessionDescription
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

interface SessionMonitorService {
    suspend fun getSessionsCount(): Long

    suspend fun getSessions(
        page: Int,
        pageSize: Int,
        sessionId: String?,
        type: Int?,
    ): PaginatedResponseData<SessionDescription>
}