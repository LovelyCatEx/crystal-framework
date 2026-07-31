package com.lovelycatv.crystalframework.monitor.controller.manager

import com.lovelycatv.crystalframework.monitor.controller.vo.BusinessStatsVO
import com.lovelycatv.crystalframework.monitor.controller.vo.DashboardStatsVO
import com.lovelycatv.crystalframework.monitor.controller.vo.SystemMetricsVO
import com.lovelycatv.crystalframework.monitor.service.DashboardService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/monitor/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    @RequiresAuthority(
        allOf = [
            "system.dashboard.business.statistics.read",
            "system.dashboard.system.metrics.read",
        ],
        scope = ResourceScope.SYSTEM,
    )
    @GetMapping("/stats")
    suspend fun getDashboardStats(
        @RequestParam(name = "timeRange", defaultValue = "1m") timeRange: String
    ): ApiResponse<DashboardStatsVO> {
        val stats = dashboardService.getDashboardStats(timeRange)
        return ApiResponse.success(stats)
    }

    @RequiresAuthority(anyOf = ["system.dashboard.business.statistics.read"], scope = ResourceScope.SYSTEM)
    @GetMapping("/business-stats")
    suspend fun getBusinessStats(
        @RequestParam(name = "timeRange", defaultValue = "1m") timeRange: String
    ): ApiResponse<BusinessStatsVO> {
        val stats = dashboardService.getBusinessStats(timeRange)
        return ApiResponse.success(stats)
    }

    @RequiresAuthority(anyOf = ["system.dashboard.system.metrics.read"], scope = ResourceScope.SYSTEM)
    @GetMapping("/system-metrics")
    suspend fun getSystemMetrics(): ApiResponse<SystemMetricsVO> {
        val metrics = dashboardService.getSystemMetrics()
        return ApiResponse.success(metrics)
    }
}
