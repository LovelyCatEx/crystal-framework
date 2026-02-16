package com.lovelycatv.template.springboot.user.controller

import com.lovelycatv.template.springboot.shared.annotations.Unauthorized
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.user.controller.dto.UserRegisterDTO
import com.lovelycatv.template.springboot.user.service.UserService
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/{version}/user")
class UserController(
    private val userService: UserService
) {
    @Unauthorized
    @PostMapping("/register", version = "1")
    suspend fun register(
        @ModelAttribute
        dto: UserRegisterDTO
    ): ApiResponse<*> {
        userService.register(dto.username, dto.password, dto.email, dto.emailCode)

        return ApiResponse.success(null, "ok")
    }
}