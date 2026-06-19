package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberProfileRepository
import org.springframework.http.codec.multipart.FilePart

interface TenantMemberProfileService : CachedBaseService<TenantMemberProfileRepository, TenantMemberProfileEntity> {
    suspend fun getByTenantMemberId(tenantMemberId: Long): TenantMemberProfileEntity?

    suspend fun getByTenantIdAndUserId(tenantId: Long, userId: Long): TenantMemberProfileEntity?

    /**
     * Create-or-update by tenantMemberId.
     *
     * `name` (real name) is write-once: only used when creating a new row, ignored on update.
     * Other fields update only when the corresponding argument is non-null.
     */
    suspend fun upsertProfile(
        tenantId: Long,
        tenantMemberId: Long,
        memberUserId: Long,
        name: String? = null,
        phone: String? = null,
        nickname: String? = null,
        avatar: Long? = null,
        email: String? = null,
        bio: String? = null,
        gender: Int? = null,
        birthday: Long? = null,
        timezone: String? = null,
        locale: String? = null,
    ): TenantMemberProfileEntity

    suspend fun uploadAvatar(
        tenantId: Long,
        tenantMemberId: Long,
        memberUserId: Long,
        file: FilePart,
    ): TenantMemberProfileEntity
}
