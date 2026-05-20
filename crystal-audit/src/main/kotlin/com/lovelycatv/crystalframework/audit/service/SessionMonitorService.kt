package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.types.SessionDescription
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

interface SessionMonitorService {
    suspend fun getSessionsCount(): Long

    suspend fun getSessions(page: Int, pageSize: Int, sessionId: String?): PaginatedResponseData<SessionDescription>
}