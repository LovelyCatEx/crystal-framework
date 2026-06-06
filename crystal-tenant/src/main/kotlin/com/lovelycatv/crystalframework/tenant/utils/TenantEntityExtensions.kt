package com.lovelycatv.crystalframework.tenant.utils

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantProfileVO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity

class TenantEntityExtensions private constructor()

suspend fun TenantEntity.toProfileVO(fileResourceService: FileResourceService): TenantProfileVO {
    return TenantProfileVO(
        tenantId = this.id,
        ownerUserId = this.ownerUserId,
        name = this.name,
        description = this.description,
        icon = this.icon?.let { fileResourceService.getFileDownloadUrl(it) },
        status = this.status,
        tireTypeId = this.tireTypeId,
        subscribedTime = this.subscribedTime,
        expiresTime = this.expiresTime,
        contactName = this.contactName,
        contactEmail = this.contactEmail,
        contactPhone = this.contactPhone,
        address = this.address,
        createdTime = this.createdTime,
        modifiedTime = this.modifiedTime
    )
}