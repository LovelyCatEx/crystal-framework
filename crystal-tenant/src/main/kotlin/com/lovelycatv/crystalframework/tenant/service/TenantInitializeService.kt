/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service

import org.springframework.transaction.annotation.Transactional

interface TenantInitializeService {
    /**
     * Initialize tenant after created.
     *
     * Attention: calling this method will result in data loss for the tenant.
     *
     * @param tenantId TenantId
     * @param ownerUserId The owner userId of this tenant
     */
    @Transactional(rollbackFor = [Exception::class])
    suspend fun initializeTenant(tenantId: Long, ownerUserId: Long)
}