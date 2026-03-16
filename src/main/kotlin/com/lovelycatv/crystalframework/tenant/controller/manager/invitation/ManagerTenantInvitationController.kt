package com.lovelycatv.crystalframework.tenant.controller.manager.invitation

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerDeleteInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerReadInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantInvitationManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/invitation")
class ManagerTenantInvitationController(
    private val tenantInvitationManagerService: TenantInvitationManagerService,
    private val tenantMemberService: TenantMemberService
) {
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateInvitationDTO
    ): ApiResponse<*> {
        val userSelfMemberId = tenantMemberService
            .getByTenantIdAndUserId(dto.tenantId, userAuthentication.userId)
            ?.id

        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_INVITATION_CREATE)) {
            tenantInvitationManagerService.create(dto.apply {
                if (this.creatorMemberId == null) {
                    this.creatorMemberId = userSelfMemberId
                }
            })
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_INVITATION_CREATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (dto.tenantId == userAuthentication.tenantId) {
                // Lock the creator to the user in authentication
                tenantInvitationManagerService.create(dto.apply {
                    this.creatorMemberId = userSelfMemberId
                })
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(null)
    }

    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadInvitationDTO
    ): ApiResponse<*> {
        val result = if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_INVITATION_READ)) {
            tenantInvitationManagerService.query(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_INVITATION_READ_PEM)) {
            if (dto.tenantId == userAuthentication.tenantId) {
                tenantInvitationManagerService.query(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(result)
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateInvitationDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_INVITATION_UPDATE)) {
            tenantInvitationManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_INVITATION_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantInvitationManagerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                // Could not update the creator
                tenantInvitationManagerService.update(dto.apply {
                    this.creatorMemberId = null
                })
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteInvitationDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_INVITATION_DELETE)) {
            tenantInvitationManagerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_INVITATION_DELETE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantInvitationManagerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                tenantInvitationManagerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(null)
    }
}