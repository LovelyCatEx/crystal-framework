package com.lovelycatv.template.springboot.rbac.controller.manager

import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerDeletePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.service.UserPermissionManagerService
import com.lovelycatv.template.springboot.rbac.service.UserPermissionService
import com.lovelycatv.template.springboot.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/manager/permission")
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
        dto: BaseManagerReadDTO
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