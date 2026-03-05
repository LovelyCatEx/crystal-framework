package com.lovelycatv.crystalframework.user.controller

import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.controller.dto.BindOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.dto.RegisterFromOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.dto.UnbindOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.vo.UserOAuthAccountVO
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.service.UserService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/user/oauth")
class OAuthAccountController(
    private val userService: UserService,
    private val userAuthorizationService: UserAuthorizationService,
    private val oAuthAccountService: OAuthAccountService,
) {
    @Unauthorized
    @PostMapping("/bindOAuthAccount")
    suspend fun bindOAuthAccount(
        userAuthentication: UserAuthentication?,
        @ModelAttribute
        @Valid
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

    @PostMapping("/unbind")
    suspend fun unbindOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UnbindOAuthAccountDTO
    ): ApiResponse<*> {
        oAuthAccountService.unbindUser(dto.oauthAccountId, userAuthentication.userId)

        return ApiResponse.success(null)
    }

    @Unauthorized
    @PostMapping("/registerFromOAuthAccount")
    suspend fun registerFromOAuthAccount(
        @ModelAttribute
        @Valid
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

    @GetMapping("/accounts")
    suspend fun getUserOAuthAccounts(
        userAuthentication: UserAuthentication,
    ): ApiResponse<*> {
        return ApiResponse.success(
            oAuthAccountService
                .getUserOAuthAccounts(userAuthentication.userId)
                .map {
                    UserOAuthAccountVO(
                        id = it.id,
                        platformId = it.platform,
                        nickname = it.nickname,
                        avatar = it.avatar,
                    )
                }
        )
    }
}