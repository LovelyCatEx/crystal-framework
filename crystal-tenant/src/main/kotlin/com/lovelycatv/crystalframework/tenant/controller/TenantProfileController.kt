package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.dto.UpdateTenantProfileDTO
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantProfileVO
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import jakarta.validation.Valid
import org.springframework.http.codec.multipart.FilePart
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/profile")
class TenantProfileController(
    private val tenantService: TenantService,
) {
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_TENANTS,
    )
    @GetMapping
    suspend fun getTenantProfile(
        userAuthentication: UserAuthentication,
        @RequestParam(required = false)
        tenantId: Long?
    ): ApiResponse<TenantProfileVO> {
        val targetTenantId = if (tenantId != null && tenantId > 0) {
            tenantId
        } else {
            userAuthentication.tenantId
                ?: throw BusinessException("invalid tenant authentication")
        }

        return ApiResponse.success(tenantService.getTenantProfile(targetTenantId, userAuthentication))
    }

    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_TENANTS,
        resourceIds = "#userAuthentication.tenantId",
    )
    @RequiresAuthority(anyOf = ["i.tenant.profile.update"], scope = ResourceScope.TENANT)
    @PostMapping("/update")
    suspend fun updateTenantProfile(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UpdateTenantProfileDTO
    ): ApiResponse<*> {
        userAuthentication.assertTenantIdNotNull()
        tenantService.updateTenantProfile(userAuthentication.tenantId!!, dto)
        return ApiResponse.success(null)
    }

    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_TENANTS,
        resourceIds = "#userAuthentication.tenantId",
    )
    @RequiresAuthority(anyOf = ["i.tenant.profile.update"], scope = ResourceScope.TENANT)
    @PostMapping("/uploadIcon")
    suspend fun uploadTenantIcon(
        userAuthentication: UserAuthentication,
        @RequestPart("file") file: FilePart
    ): ApiResponse<*> {
        userAuthentication.assertTenantIdNotNull()
        tenantService.uploadTenantIcon(userAuthentication.userId, userAuthentication.tenantId!!, file)
        return ApiResponse.success(null)
    }
}
