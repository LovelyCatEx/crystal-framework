/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TenantMemberProfileRepository : BaseRepository<TenantMemberProfileEntity> {
    fun findByTenantIdAndTenantMemberId(tenantId: Long, tenantMemberId: Long): Mono<TenantMemberProfileEntity>

    fun findByTenantIdAndMemberUserId(tenantId: Long, memberUserId: Long): Mono<TenantMemberProfileEntity>
}
