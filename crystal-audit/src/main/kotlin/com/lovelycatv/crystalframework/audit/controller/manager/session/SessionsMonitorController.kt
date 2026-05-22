package com.lovelycatv.crystalframework.audit.controller.manager.session

import com.lovelycatv.crystalframework.audit.controller.manager.session.dto.SessionSearchDTO
import com.lovelycatv.crystalframework.audit.service.SessionMonitorService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/monitor/session")
class SessionsMonitorController(
    private val sessionMonitorService: SessionMonitorService
) {
    @PreAuthorize("hasAuthority('${SystemPermission.ACTION_MONITOR_SESSIONS_READ}')")
    @GetMapping("/online")
    suspend fun getSessions(@Valid dto: SessionSearchDTO): ApiResponse<*> {
        return ApiResponse.success(
            sessionMonitorService.getSessions(dto.page, dto.pageSize, dto.sessionId)
        )
    }
}