package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.controller.dto.SystemInitializeDTO
import com.lovelycatv.crystalframework.system.service.SystemInitializeService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/system")
class SystemInitializeController(
    private val systemInitializeService: SystemInitializeService
) {
    @Unauthorized
    @GetMapping("/initialize/status")
    suspend fun getInitializeStatus(): ApiResponse<*> {
        return ApiResponse.success(
            mapOf(
                "initialized" to systemInitializeService.isSystemInitialized()
            )
        )
    }

    @Unauthorized
    @PostMapping("/initialize")
    suspend fun initializeSystem(
        @RequestBody
        @Valid
        dto: SystemInitializeDTO
    ): ApiResponse<*> {
        systemInitializeService.initializeSystem(
            username = dto.username,
            password = dto.password,
            email = dto.email,
            smtpHost = dto.smtpHost,
            smtpPort = dto.smtpPort,
            smtpUsername = dto.smtpUsername,
            smtpPassword = dto.smtpPassword,
            fromEmail = dto.fromEmail,
            fromName = dto.fromName
        )
        return ApiResponse.success(null, "System initialized successfully")
    }
}