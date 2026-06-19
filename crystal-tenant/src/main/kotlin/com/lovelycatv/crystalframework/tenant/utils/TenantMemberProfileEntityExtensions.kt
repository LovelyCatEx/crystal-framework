package com.lovelycatv.crystalframework.tenant.utils

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantMemberProfileVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity

class TenantMemberProfileEntityExtensions private constructor()

suspend fun TenantMemberProfileEntity.toProfileVO(
    fileResourceService: FileResourceService,
    fullAccess: Boolean = true
): TenantMemberProfileVO {
    return TenantMemberProfileVO(
        id = this.id,
        tenantId = this.tenantId,
        tenantMemberId = this.tenantMemberId,
        memberUserId = this.memberUserId,
        name = this.name,
        phone = if (fullAccess) this.phone else "",
        nickname = this.nickname,
        avatar = fileResourceService.getFileDownloadUrl(this.avatar),
        email = if (fullAccess) this.email else null,
        bio = this.bio,
        gender = this.gender,
        birthday = if (fullAccess) this.birthday else null,
        timezone = this.timezone,
        locale = this.locale,
        createdTime = this.createdTime,
        modifiedTime = this.modifiedTime,
    )
}
