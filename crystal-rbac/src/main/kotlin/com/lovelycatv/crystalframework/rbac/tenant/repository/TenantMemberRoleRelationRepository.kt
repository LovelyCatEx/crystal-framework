/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantMemberRoleRelationEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantMemberRoleRelationRepository : BaseRepository<TenantMemberRoleRelationEntity> {
    fun findAllByMemberId(memberId: Long): Flux<TenantMemberRoleRelationEntity>

    fun findAllByRoleId(roleId: Long): Flux<TenantMemberRoleRelationEntity>

    fun findAllByRoleIdIn(roleId: Collection<Long>): Flux<TenantMemberRoleRelationEntity>

    fun findByMemberIdAndRoleId(memberId: Long, roleId: Long): Mono<TenantMemberRoleRelationEntity>

    fun deleteByRoleIdIn(roleIds: Collection<Long>): Mono<Void>

    fun deleteByMemberIdIn(memberIds: Collection<Long>): Mono<Void>
}
