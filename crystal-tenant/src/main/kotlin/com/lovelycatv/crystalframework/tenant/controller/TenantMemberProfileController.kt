package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.dto.UpsertTenantMemberProfileDTO
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantMemberProfileVO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberProfileService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/me/tenant-profile")
class TenantMemberProfileController(
    private val tenantMemberProfileService: TenantMemberProfileService,
) {
    @GetMapping("")
    suspend fun getMyTenantUserProfile(
        userAuthentication: UserAuthentication,
    ): ApiResponse<TenantMemberProfileVO?> {
        val tenantMemberId = userAuthentication.tenantMemberId
            ?: throw BusinessException("invalid tenant authentication")

        val profile = tenantMemberProfileService.getByTenantMemberId(tenantMemberId)
        return ApiResponse.success(profile?.let { TenantMemberProfileVO.fromEntity(it) })
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
