package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.controller.dto.UpsertTenantMemberProfileDTO
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantMemberProfileVO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberProfileService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/me/tenant-profile")
class TenantMemberProfileController(
    private val tenantMemberProfileService: TenantMemberProfileService,
) {
    @GetMapping("")
    suspend fun getTenantMemberProfile(
        userAuthentication: UserAuthentication,
        @RequestParam("memberId", required = false, defaultValue = "0")
        memberId: Long?
    ): ApiResponse<TenantMemberProfileVO?> {
        val targetMemberId = if (memberId != null && memberId > 0) {
            memberId
        } else {
            userAuthentication.tenantMemberId
                ?: throw BusinessException("invalid tenant authentication")
        }

        val fullAccess = targetMemberId == userAuthentication.tenantMemberId
                || RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_READ_PEM)

        val profile = tenantMemberProfileService.getByTenantMemberId(targetMemberId)
            ?: return ApiResponse.success(null)

        return ApiResponse.success(TenantMemberProfileVO.fromEntity(profile, fullAccess))
    }

    @PostMapping("/upsert")
    suspend fun upsertMyTenantUserProfile(
        userAuthentication: UserAuthentication,
        @ModelAttribute @Valid dto: UpsertTenantMemberProfileDTO,
    ): ApiResponse<TenantMemberProfileVO> {
        val tenantId = userAuthentication.assertTenantIdNotNull()
        val tenantMemberId = userAuthentication.tenantMemberId
            ?: throw BusinessException("invalid tenant authentication")

        val saved = tenantMemberProfileService.upsertProfile(
            tenantId = tenantId,
            tenantMemberId = tenantMemberId,
            memberUserId = userAuthentication.userId,
            phone = dto.phone,
            nickname = dto.nickname,
            avatar = dto.avatar,
            email = dto.email,
            bio = dto.bio,
            gender = dto.gender,
            birthday = dto.birthday,
            timezone = dto.timezone,
            locale = dto.locale,
        )

        return ApiResponse.success(TenantMemberProfileVO.fromEntity(saved))
    }
}
