package com.lovelycatv.crystalframework.rbac.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-permission")
class ManagerUserPermissionController(
    private val userPermissionManagerService: UserPermissionManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllPermissions(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(userPermissionManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createPermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreatePermissionDTO
    ): ApiResponse<*> {
        userPermissionManagerService.create(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readPermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadPermissionDTO
    ): ApiResponse<*> {
        return ApiResponse.success(userPermissionManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updatePermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdatePermissionDTO
    ): ApiResponse<*> {
        userPermissionManagerService.update(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deletePermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeletePermissionDTO
    ): ApiResponse<*> {
        userPermissionManagerService.deleteByDTO(dto)

        return ApiResponse.success(null)
    }
}