package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository

interface TenantMemberService : CachedBaseService<TenantMemberRepository, TenantMemberEntity> {
    suspend fun getByTenantIdAndUserId(tenantId: Long, userId: Long): TenantMemberEntity?

    suspend fun transformTenantMemberVO(tenantMemberEntity: TenantMemberEntity): TenantMemberVO

    /** All member user ids belonging to the given tenant. */
    suspend fun listMemberUserIds(tenantId: Long): List<Long>

    /** All tenant ids the given user is a member of. */
    suspend fun listTenantIdsByUserId(userId: Long): List<Long>
}
