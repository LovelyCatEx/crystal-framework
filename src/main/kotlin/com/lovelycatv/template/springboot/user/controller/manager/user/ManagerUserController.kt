package com.lovelycatv.template.springboot.user.controller.manager.user

import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.template.springboot.user.service.UserManagerService
import com.lovelycatv.template.springboot.shared.constants.GlobalConstants
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user")
class ManagerUserController(
    private val userManagerService: UserManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllPermissions(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(userManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createUser(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerCreateUserDTO
    ): ApiResponse<*> {
        userManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readUser(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerReadUserDTO
    ): ApiResponse<*> {
        return ApiResponse.success(userManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateUser(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerUpdateUserDTO
    ): ApiResponse<*> {
        userManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteUser(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        dto: ManagerDeleteUserDTO
    ): ApiResponse<*> {
        userManagerService.delete(dto.id)
        return ApiResponse.success(null)
    }
}
