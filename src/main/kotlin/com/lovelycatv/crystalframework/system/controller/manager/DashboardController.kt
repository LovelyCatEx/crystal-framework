package com.lovelycatv.crystalframework.system.controller.manager

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.controller.vo.BusinessStatsVO
import com.lovelycatv.crystalframework.system.controller.vo.DashboardStatsVO
import com.lovelycatv.crystalframework.system.controller.vo.SystemMetricsVO
import com.lovelycatv.crystalframework.system.service.DashboardService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    @GetMapping("/stats")
    suspend fun getDashboardStats(
        @RequestParam(name = "timeRange", defaultValue = "1m") timeRange: String
    ): ApiResponse<DashboardStatsVO> {
        val stats = dashboardService.getDashboardStats(timeRange)
        return ApiResponse.success(stats)
    }

    @GetMapping("/business-stats")
    suspend fun getBusinessStats(
        @RequestParam(name = "timeRange", defaultValue = "1m") timeRange: String
    ): ApiResponse<BusinessStatsVO> {
        val stats = dashboardService.getBusinessStats(timeRange)
        return ApiResponse.success(stats)
    }

    @GetMapping("/system-metrics")
    suspend fun getSystemMetrics(): ApiResponse<SystemMetricsVO> {
        val metrics = dashboardService.getSystemMetrics()
        return ApiResponse.success(metrics)
    }
}
