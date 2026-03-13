package com.lovelycatv.crystalframework.user.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.user.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/user/menus")
class UserAccessibleMenuController(
    private val userRbacQueryService: UserRbacQueryService
) {
    @GetMapping("/list")
    suspend fun listAccessibleMenus(userAuthentication: UserAuthentication): ApiResponse<*> {
        return ApiResponse.success(
            userRbacQueryService
                .getUserRbacAccessInfo(userAuthentication.userId)
                .paths
                .mapNotNull { it.path } + userRbacQueryService
                .getUserTenantRbacAccessInfo(userAuthentication.userId)
                .tenants
                .filter { it.tenantId == userAuthentication.tenantId }
                .flatMap { it.permissions.map { it.path } }
                .filterNotNull()
        )
    }
}