package com.lovelycatv.crystalframework.audit.controller.manager

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/sessions")
class SessionsMonitorController {
    @GetMapping("/online")
    suspend fun getSessions(): ApiResponse<*> {
        return ApiResponse.success(mapOf("a" to "b"))
    }
}