package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.dto.UpdateTenantProfileDTO
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.utils.toProfileVO
import jakarta.validation.Valid
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/profile")
class TenantProfileController(
    private val tenantService: TenantService,
    private val fileResourceService: FileResourceService
) {
    @GetMapping
    suspend fun getTenantProfile(
        userAuthentication: UserAuthentication,
        @RequestParam(required = false)
        tenantId: Long?
    ): ApiResponse<*> {
        val tenant = tenantService.getByIdOrNull(
            if (tenantId != null && tenantId > 0)
                tenantId
            else
                userAuthentication.tenantId
        ) ?: throw BusinessException("Tenant not found")

        val tenantProfileVO = tenant.toProfileVO(fileResourceService)

        return ApiResponse.success(tenantProfileVO.apply {
            if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_PROFILE_READ_PEM)) {
                // Do nothing
            } else {
                this.ownerUserId = null
                this.tireTypeId = null
                this.subscribedTime = null
                this.expiresTime = null

                if (!RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_PROFILE_READ_BASIC_PEM)) {
                    this.contactName = null
                    this.contactEmail = null
                    this.contactPhone = null
                }
            }
        })
    }

    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
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

    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
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
