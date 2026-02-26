package com.lovelycatv.template.springboot.user.controller

import com.lovelycatv.template.springboot.shared.constants.GlobalConstants
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import com.lovelycatv.template.springboot.user.service.UserService
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