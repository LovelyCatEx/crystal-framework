package com.lovelycatv.template.springboot.user.controller

import com.lovelycatv.template.springboot.shared.annotations.Unauthorized
import com.lovelycatv.template.springboot.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import com.lovelycatv.template.springboot.user.controller.dto.UserRegisterDTO
import com.lovelycatv.template.springboot.user.controller.dto.RequestRegisterEmailCodeDTO
import com.lovelycatv.template.springboot.user.controller.dto.RequestResetPasswordEmailCodeDTO
import com.lovelycatv.template.springboot.user.controller.dto.ResetPasswordDTO
import com.lovelycatv.template.springboot.user.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/user")
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

    @Unauthorized
    @PostMapping("/requestRegisterEmailCode", version = "1")
    suspend fun requestRegisterEmailCode(
        @ModelAttribute
        dto: RequestRegisterEmailCodeDTO
    ): ApiResponse<*> {
        userService.requestRegisterEmailConfirmationCode(dto.email)

        return ApiResponse.success(null, "ok")
    }

    @Unauthorized
    @PostMapping("/resetPassword")
    suspend fun resetPassword(
      @ModelAttribute
      dto: ResetPasswordDTO
    ): ApiResponse<*> {
        userService.resetPassword(dto.email, dto.emailCode, dto.newPassword)

        return ApiResponse.success(null)
    }

    @Unauthorized
    @PostMapping("/requestPasswordResetEmailCode")
    suspend fun requestPasswordResetEmailCode(
        @ModelAttribute
        dto: RequestResetPasswordEmailCodeDTO
    ): ApiResponse<*> {
        userService.requestResetPasswordEmailConfirmationCode(dto.email)

        return ApiResponse.success(null)
    }

    @Unauthorized
    @GetMapping("/profile")
    suspend fun getUserProfile(
        userAuthentication: UserAuthentication?,
        @RequestParam("id", required = false, defaultValue = "0")
        userId: Long?
    ): ApiResponse<*> {
        val targetUserId = if (userId != null && userId > 0) {
            userId
        } else {
            userAuthentication?.userId ?: throw BusinessException("unknown target user id")
        }

        return ApiResponse.success(
            userService.getUserProfileVO(
                userId = targetUserId,
                fullAccess = targetUserId == userAuthentication?.userId
            )
        )
    }
}