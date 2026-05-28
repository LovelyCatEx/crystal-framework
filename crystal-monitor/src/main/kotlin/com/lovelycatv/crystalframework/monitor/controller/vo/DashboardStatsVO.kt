package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * Dashboard statistics response VO
 */
data class DashboardStatsVO(
    val businessStats: BusinessStatsVO,
    val systemMetrics: SystemMetricsVO
)
