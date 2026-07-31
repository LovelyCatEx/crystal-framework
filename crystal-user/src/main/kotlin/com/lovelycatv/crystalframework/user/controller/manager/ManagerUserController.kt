package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerRefreshUserAuthoritiesDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserManagerService
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
            throw ForbiddenException()
        }
        dto.userIds.forEach { userRbacQueryService.clearUserAuthoritiesCache(it) }
        return ApiResponse.success(mapOf("refreshed" to dto.userIds.size))
    }
}
