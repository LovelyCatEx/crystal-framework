package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.system.controller.vo.BusinessStatsVO
import com.lovelycatv.crystalframework.system.controller.vo.DashboardStatsVO
import com.lovelycatv.crystalframework.system.controller.vo.SystemMetricsVO

/**
 * Dashboard Statistics Service
 */
interface DashboardService {
    /**
     * Get dashboard statistics data
     *
     * @param timeRange Time range format: 1d, 3d, 5d, 1w, 2w, 1m, 3m, 6m, 1y
     * @return Dashboard statistics data
     */
    suspend fun getDashboardStats(timeRange: String): DashboardStatsVO

    /**
     * Get business statistics data (8 cards)
     *
     * @param timeRange Time range format: 1d, 3d, 5d, 1w, 2w, 1m, 3m, 6m, 1y
     * @return Business statistics data
     */
    suspend fun getBusinessStats(timeRange: String): BusinessStatsVO

    /**
     * Get system resource monitoring data
     *
     * @return System resource monitoring data
     */
    suspend fun getSystemMetrics(): SystemMetricsVO
}