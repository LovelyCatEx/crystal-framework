package com.lovelycatv.crystalframework.audit.controller.manager.session

import com.lovelycatv.crystalframework.audit.controller.manager.session.dto.SessionSearchDTO
import com.lovelycatv.crystalframework.audit.service.SessionMonitorService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import jakarta.validation.Valid
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
    @RequiresAuthority(anyOf = ["system.monitor.sessions.read"], scope = ResourceScope.SYSTEM)
    @GetMapping("/online")
    suspend fun getSessions(@Valid dto: SessionSearchDTO): ApiResponse<*> {
        return ApiResponse.success(
            sessionMonitorService.getSessions(dto.page, dto.pageSize, dto.sessionId, dto.type)
        )
    }
}