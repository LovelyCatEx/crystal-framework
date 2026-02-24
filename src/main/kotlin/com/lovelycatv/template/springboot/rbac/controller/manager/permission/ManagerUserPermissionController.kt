package com.lovelycatv.template.springboot.rbac.controller.manager.permission

import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.shared.constants.GlobalConstants
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-permission")
class ManagerUserPermissionController(
    private val userPermissionManagerService: UserPermissionManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createPermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
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
        dto: ManagerReadPermissionDTO
    ): ApiResponse<*> {
        return ApiResponse.success(userPermissionManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_PERMISSION_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updatePermission(
        userAuthentication: UserAuthentication,
        @ModelAttribute
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
        dto: ManagerDeletePermissionDTO
    ): ApiResponse<*> {
        userPermissionManagerService.delete(dto.id)

        return ApiResponse.success(null)
    }
}