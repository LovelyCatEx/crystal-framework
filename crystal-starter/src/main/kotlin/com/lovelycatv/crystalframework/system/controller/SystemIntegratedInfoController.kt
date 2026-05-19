package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.controller.vo.SystemIntegratedInfoVO
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/system")
class SystemIntegratedInfoController(
    private val readinessController: ReadinessController,
    private val systemSettingsService: SystemSettingsService
) {
    @Unauthorized
    @GetMapping("/integratedInfo")
    suspend fun getSystemIntegratedInfo(): ApiResponse<*> {
        val systemSettings = systemSettingsService.getSystemSettings()
        return ApiResponse.success(
            SystemIntegratedInfoVO(
                maintenance = readinessController.getSystemMaintenance().data!!,
                waterMark = SystemIntegratedInfoVO.WaterMark(
                    enabled = systemSettings.basic.waterMark.enabled,
                    type = systemSettings.basic.waterMark.type,
                    customValue = systemSettings.basic.waterMark.customValue,
                    fontColor = systemSettings.basic.waterMark.fontColor
                )
            )
        )
    }
}