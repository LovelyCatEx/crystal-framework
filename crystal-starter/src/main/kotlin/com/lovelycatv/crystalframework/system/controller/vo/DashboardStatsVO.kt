package com.lovelycatv.crystalframework.system.controller.vo

/**
 * Dashboard statistics response VO
 */
data class DashboardStatsVO(
    val businessStats: BusinessStatsVO,
    val systemMetrics: SystemMetricsVO
)
