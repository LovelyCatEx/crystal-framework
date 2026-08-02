package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.user.service.UserForceLogoutService
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerBanUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerForceLogoutUsersDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerRefreshUserAuthoritiesDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerSetUserEnabledDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUnbanUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.crystalframework.user.service.manager.UserBanRecordManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user")
class ManagerUserController(
    managerService: UserManagerService,
    private val userRbacQueryService: UserRbacQueryService,
    private val userForceLogoutService: UserForceLogoutService,
    private val userBanRecordManagerService: UserBanRecordManagerService,
) : StandardManagerController<
        UserManagerService,
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_USER_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_USER_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_USER_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_USER_DELETE.name,
    ),
) {
    /**
     * Enrich the paginated user list with the derived [UserEntity.getBanned] flag so the manager UI
     * can show a ban / unban action per row. The banned set is fetched in a single batched query to
     * avoid N+1.
     */
    override suspend fun buildReadResponse(
        dto: ManagerReadUserDTO,
        userAuthentication: UserAuthentication,
    ): Any {
        val page = managerService.query(dto)
        val bannedUserIds = userBanRecordManagerService.getActivelyBannedUserIds(page.records.map { it.id })
        page.records.forEach { it.setBanned(bannedUserIds.contains(it.id)) }
        return page
    }

    @Suppress("UNUSED_PARAMETER")
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USERS,
        resourceIds = "#dto.userIds",
    )
    @PostMapping("/refresh-authority")
    suspend fun refreshAuthority(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerRefreshUserAuthoritiesDTO,
    ): ApiResponse<*> {
        if (!RbacUtils.hasAnyAuthority(SystemPermission.ACTION_SYSTEM_USER_REFRESH_AUTHORITY.name)) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = listOf(SystemPermission.ACTION_SYSTEM_USER_REFRESH_AUTHORITY.name),
                scope = ResourceScope.SYSTEM,
            ))
        }
        dto.userIds.forEach { userRbacQueryService.clearUserAuthoritiesCache(it) }
        return ApiResponse.success(mapOf("refreshed" to dto.userIds.size))
    }

    @Suppress("UNUSED_PARAMETER")
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USERS,
        resourceIds = "#dto.userIds",
    )
    @RequiresAuthority(anyOf = ["system.user.forceLogout"], scope = ResourceScope.SYSTEM)
    @PostMapping("/force-logout")
    suspend fun forceLogout(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerForceLogoutUsersDTO,
    ): ApiResponse<*> {
        dto.userIds.forEach { userForceLogoutService.markForceLogout(it) }
        return ApiResponse.success(mapOf("forced" to dto.userIds.size))
    }

    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USERS,
        resourceIds = "#dto.userId",
    )
    @RequiresAuthority(anyOf = ["system.user.ban"], scope = ResourceScope.SYSTEM)
    @PostMapping("/ban")
    suspend fun ban(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerBanUserDTO,
    ): ApiResponse<*> {
        userBanRecordManagerService.banUser(
            userId = dto.userId,
            reason = dto.reason,
            banUntil = dto.banUntil,
            operatorUserId = userAuthentication.userId,
        )
        userForceLogoutService.markForceLogout(dto.userId)
        return ApiResponse.success(mapOf("banned" to dto.userId))
    }

    @Suppress("UNUSED_PARAMETER")
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USERS,
        resourceIds = "#dto.userId",
    )
    @RequiresAuthority(anyOf = ["system.user.unban"], scope = ResourceScope.SYSTEM)
    @PostMapping("/unban")
    suspend fun unban(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUnbanUserDTO,
    ): ApiResponse<*> {
        userBanRecordManagerService.unbanUser(dto.userId)
        return ApiResponse.success(mapOf("unbanned" to dto.userId))
    }

    @Suppress("UNUSED_PARAMETER")
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USERS,
        resourceIds = "#dto.userId",
    )
    @RequiresAuthority(anyOf = ["system.user.setEnabled"], scope = ResourceScope.SYSTEM)
    @PostMapping("/set-enabled")
    suspend fun setEnabled(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerSetUserEnabledDTO,
    ): ApiResponse<*> {
        managerService.setEnabled(dto.userId, dto.enabled)
        if (!dto.enabled) {
            userForceLogoutService.markForceLogout(dto.userId)
        }
        return ApiResponse.success(mapOf("userId" to dto.userId, "enabled" to dto.enabled))
    }
}
