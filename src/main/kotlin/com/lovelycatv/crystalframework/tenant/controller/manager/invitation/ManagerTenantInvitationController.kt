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
import com.lovelycatv.crystalframework.tenant.controller.manager.StandardTenantManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerDeleteInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerReadInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantInvitationManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/invitation")
class ManagerTenantInvitationController(
    private val tenantInvitationManagerService: TenantInvitationManagerService,
    private val tenantMemberService: TenantMemberService
) : StandardTenantManagerController<
        TenantInvitationManagerService,
        TenantInvitationRepository,
        TenantInvitationEntity,
        ManagerCreateInvitationDTO,
        ManagerReadInvitationDTO,
        ManagerUpdateInvitationDTO,
        ManagerDeleteInvitationDTO
>(
    tenantInvitationManagerService,
    createPermission = SystemPermission.ACTION_TENANT_INVITATION_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_INVITATION_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_INVITATION_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_INVITATION_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_INVITATION_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_INVITATION_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_INVITATION_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_INVITATION_DELETE_PEM
) {
    override suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: ManagerCreateInvitationDTO
    ): ApiResponse<*>? {
        val userSelfMemberId = tenantMemberService
            .getByTenantIdAndUserId(dto.tenantId, userAuthentication.userId)
            ?.id

        if (RbacUtils.hasAuthority(this.createPermission)) {
            // System-level callers may attribute the invitation to any member, but that
            // member must actually belong to the target tenant.
            dto.creatorMemberId?.let { memberId ->
                val member = tenantMemberService.getByIdOrNull(memberId)
                    ?: throw BusinessException("creator member $memberId does not exist")
                if (member.tenantId != dto.tenantId) {
                    throw BusinessException("creator member $memberId does not belong to tenant ${dto.tenantId}")
                }
            }
            tenantInvitationManagerService.create(dto.apply {
                if (this.creatorMemberId == null) {
                    this.creatorMemberId = userSelfMemberId
                }
            })
        } else if (RbacUtils.hasAuthority(this.scopedCreatePermission)) {
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

    override suspend fun customUpdate(
        userAuthentication: UserAuthentication,
        dto: ManagerUpdateInvitationDTO
    ): ApiResponse<*>? {
        if (RbacUtils.hasAuthority(this.updatePermission)) {
            tenantInvitationManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(this.scopedUpdatePermission)) {
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
}
