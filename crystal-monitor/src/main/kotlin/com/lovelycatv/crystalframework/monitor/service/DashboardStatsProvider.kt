/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.service

import reactor.core.publisher.Mono

/**
 * Provider interface for business statistics counts.
 * Implemented in crystal-starter to avoid circular dependency.
 */
interface DashboardStatsProvider {
    fun countUsers(startTime: Long, endTime: Long): Mono<Long>
    fun countTenants(startTime: Long, endTime: Long): Mono<Long>
    fun countTenantMembers(startTime: Long, endTime: Long): Mono<Long>
    fun countFileResources(startTime: Long, endTime: Long): Mono<Long>
    fun countTenantInvitations(startTime: Long, endTime: Long): Mono<Long>
    fun countTenantInvitationRecords(startTime: Long, endTime: Long): Mono<Long>
    fun countOAuthAccounts(startTime: Long, endTime: Long): Mono<Long>
    fun countMailSent(startTime: Long, endTime: Long): Mono<Long>
}
