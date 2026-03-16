package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.dto.AcceptTenantInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantInvitationVO
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentService
import com.lovelycatv.crystalframework.tenant.service.TenantInvitationService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/invitation")
class TenantInvitationController(
    private val tenantInvitationService: TenantInvitationService,
    private val tenantDepartmentService: TenantDepartmentService,
) {
    @GetMapping("/query")
    suspend fun queryTenantInvitation(
        userAuthentication: UserAuthentication,
        @RequestParam
        code: String,
    ): ApiResponse<*> {
        val invitation = tenantInvitationService.getInvitationByCode(code)
        val department = invitation.departmentId?.let {
            tenantDepartmentService.getByIdOrNull(it)
        }

        return ApiResponse.success(
            TenantInvitationVO(
                tenantId = invitation.tenantId,
                expiresAt = invitation.expiresTime,
                departmentName = department?.name,
                reachedUsageLimit = tenantInvitationService.isOverInvitationCount(invitation)
            )
        )
    }

    @PostMapping("/accept")
    suspend fun acceptTenantInvitation(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: AcceptTenantInvitationDTO
    ): ApiResponse<*> {
        tenantInvitationService.acceptInvitation(
            userAuthentication.userId,
            dto.invitationCode,
            dto.realName,
            dto.phoneNumber
        )

        return ApiResponse.success(null)
    }
}