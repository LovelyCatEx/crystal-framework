package com.lovelycatv.crystalframework.user.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/user/menus")
class UserAccessibleMenuController(private val userService: UserService) {
    @GetMapping("/list")
    suspend fun listAccessibleMenus(userAuthentication: UserAuthentication): ApiResponse<*> {
        return ApiResponse.success(
            userService
                .getUserRbacAccessInfo(userAuthentication.userId)
                .paths
                .mapNotNull { it.path }
        )
    }
}