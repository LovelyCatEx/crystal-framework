package com.lovelycatv.crystalframework.system.controller.vo

/**
 * Business statistics data VO
 */
data class BusinessStatsVO(
    val totalUsers: StatItem,
    val totalTenants: StatItem,
    val totalTenantMembers: StatItem,
    val totalFileResources: StatItem,
    val totalMailSent: StatItem,
    val totalInvitations: StatItem,
    val totalInvitationRecords: StatItem,
    val totalOAuthAccounts: StatItem
) {
    /**
     * Statistical item with value and change information
     */
    data class StatItem(
        val value: Long,
        val change: Long,
        val changePercent: Double
    )
}
