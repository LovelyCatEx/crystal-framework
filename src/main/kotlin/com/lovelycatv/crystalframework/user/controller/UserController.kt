package com.lovelycatv.crystalframework.user.controller

import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.controller.dto.*
import com.lovelycatv.crystalframework.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.codec.multipart.FilePart
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/user")
class UserController(
    private val userService: UserService,
    private val userAuthorizationService: UserAuthorizationService
) {
    @Unauthorized
    @PostMapping("/register", version = "1")
    suspend fun register(
        @ModelAttribute
        @Valid
        dto: UserRegisterDTO
    ): ApiResponse<*> {
        userService.register(dto.username, dto.password, dto.email, dto.emailCode)

        return ApiResponse.success(null, "ok")
    }

    @Unauthorized
    @PostMapping("/requestRegisterEmailCode", version = "1")
    suspend fun requestRegisterEmailCode(
        @ModelAttribute
        @Valid
        dto: RequestRegisterEmailCodeDTO
    ): ApiResponse<*> {
        userService.requestRegisterEmailConfirmationCode(dto.email)

        return ApiResponse.success(null, "ok")
    }

    @Unauthorized
    @PostMapping("/resetPassword")
    suspend fun resetPassword(
      @ModelAttribute
      @Valid
      dto: ResetPasswordDTO
    ): ApiResponse<*> {
        userService.resetPassword(dto.email, dto.emailCode, dto.newPassword)

        return ApiResponse.success(null)
    }

    @Unauthorized
    @PostMapping("/requestPasswordResetEmailCode")
    suspend fun requestPasswordResetEmailCode(
        @ModelAttribute
        @Valid
        dto: RequestResetPasswordEmailCodeDTO
    ): ApiResponse<*> {
        userService.requestResetPasswordEmailConfirmationCode(dto.email)

        return ApiResponse.success(null)
    }

    @PostMapping("/resetEmail")
    suspend fun resetEmail(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ResetEmailDTO
    ): ApiResponse<*> {
        userService.resetEmailAddress(userAuthentication.userId, dto.emailCode, dto.newEmail)

        return ApiResponse.success(null)
    }

    @PostMapping("/requestResetEmailAddressEmailCode")
    suspend fun requestPasswordResetEmailCode(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        userService.requestResetEmailAddressEmailConfirmationCode(userAuthentication.userId)

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

    @PostMapping("/profile")
    suspend fun updateUserProfile(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UpdateUserProfileDTO
    ): ApiResponse<*> {
        userService.updateUserProfile(userAuthentication.userId, dto)

        return ApiResponse.success(null)
    }

    @PostMapping("/uploadAvatar")
    suspend fun uploadAvatar(
        userAuthentication: UserAuthentication,
        @RequestPart("file")
        file: FilePart
    ): ApiResponse<*> {
        userService.uploadAvatar(
            userId = userAuthentication.userId,
            file = file
        )

        return ApiResponse.success(null)
    }

    @Unauthorized
    @PostMapping("/bindOAuthAccount")
    suspend fun bindOAuthAccount(
        userAuthentication: UserAuthentication?,
        @ModelAttribute
        dto: BindOAuthAccountDTO
    ): ApiResponse<*> {
        if (userAuthentication == null && dto.username == null && dto.password == null) {
            throw BusinessException("authentication and params cannot both be null")
        }

        val targetUser = if (userAuthentication != null) {
            if (dto.username == null || dto.password == null) {
                userService.bindUserFromOAuthAccount(
                    oauthAccountId = dto.oauthAccountId,
                    userId = userAuthentication.userId
                )
            } else {
                userService.bindUserFromOAuthAccount(
                    oauthAccountId = dto.oauthAccountId,
                    username = dto.username,
                    password = dto.password,
                )
            }
        } else {
            userService.bindUserFromOAuthAccount(
                oauthAccountId = dto.oauthAccountId,
                username = dto.username ?: throw BusinessException("unknown username"),
                password = dto.password ?: throw BusinessException("unknown password"),
            )
        }

        return ApiResponse.success(
            userAuthorizationService.buildLoginSuccessResponse(targetUser)
        )
    }

    @Unauthorized
    @PostMapping("/registerFromOAuthAccount")
    suspend fun registerFromOAuthAccount(
        @ModelAttribute
        dto: RegisterFromOAuthAccountDTO
    ): ApiResponse<*> {
        val user = userService.registerFromOAuthAccount(
            oauthAccountId = dto.oauthAccountId,
            username = dto.username,
            password = dto.password,
            nickname = dto.nickname
        )

        return ApiResponse.success(
            userAuthorizationService.buildLoginSuccessResponse(user)
        )
    }
}