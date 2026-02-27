package com.lovelycatv.crystalframework.rbac.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.service.UserRoleManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role")
class ManagerUserRoleController(
    private val userRoleManagerService: UserRoleManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllRoles(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(userRoleManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createRole(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerCreateRoleDTO
    ): ApiResponse<*> {
        userRoleManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readRole(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerReadRoleDTO
    ): ApiResponse<*> {
        return ApiResponse.success(userRoleManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateRole(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerUpdateRoleDTO
    ): ApiResponse<*> {
        userRoleManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteRole(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerDeleteRoleDTO
    ): ApiResponse<*> {
        userRoleManagerService.delete(dto.id)
        return ApiResponse.success(null)
    }
}
