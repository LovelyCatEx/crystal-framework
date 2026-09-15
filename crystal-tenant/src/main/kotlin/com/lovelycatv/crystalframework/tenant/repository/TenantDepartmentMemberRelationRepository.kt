/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDepartmentMemberRelationRepository : BaseRepository<TenantDepartmentMemberRelationEntity> {
    fun findAllByDepartmentId(departmentId: Long): Flux<TenantDepartmentMemberRelationEntity>

    fun findAllByDepartmentIdIn(departmentIds: Collection<Long>): Flux<TenantDepartmentMemberRelationEntity>

    fun findAllByMemberId(memberId: Long): Flux<TenantDepartmentMemberRelationEntity>

    fun findByDepartmentIdAndMemberId(departmentId: Long, memberId: Long): Mono<TenantDepartmentMemberRelationEntity>

    fun deleteByMemberIdIn(memberIds: Collection<Long>): Mono<Void>
}
