package com.lovelycatv.crystalframework.auth.controller

import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.auth.OAuthBindingScope
import com.lovelycatv.crystalframework.auth.controller.dto.BindOAuthAccountDTO
import com.lovelycatv.crystalframework.auth.controller.dto.BindOAuthByAccountIdDTO
import com.lovelycatv.crystalframework.user.controller.dto.RegisterFromOAuthAccountDTO
import com.lovelycatv.crystalframework.auth.controller.dto.UnbindOAuthAccountDTO
import com.lovelycatv.crystalframework.auth.controller.vo.UserOAuthAccountVO
import com.lovelycatv.crystalframework.auth.controller.vo.TenantOAuthAccountVO
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.service.UserService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

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

    /**
     * Unified OAuth binding endpoint. Binds the specified OAuth identity to the current user
     * at the given scope (SYSTEM or TENANT). The OAuth account must already exist (created by
     * the login/code-exchange flow via loginByOAuth2Code).
     */
    @PostMapping("/bindByAccountId")
    suspend fun bindOAuthByAccountId(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: BindOAuthByAccountIdDTO
    ): ApiResponse<*> {
        val account = oAuthAccountService.getByIdOrNull(dto.oauthAccountId)
            ?: throw BusinessException("OAuth account not found")

        // The identity must either be unbound or belong to the current user
        if (account.userId != null && account.userId != userAuthentication.userId) {
            throw BusinessException("This OAuth identity belongs to another user")
        }

        // Ensure system-level binding exists (required for both scopes)
        if (account.userId == null) {
            oAuthAccountService.bindUser(account.id, userAuthentication.userId)
        }

        val scope = OAuthBindingScope.getByTypeId(dto.scope)
            ?: throw BusinessException("Invalid binding scope: ${dto.scope}")

        return when (scope) {
            OAuthBindingScope.SYSTEM -> {
                ApiResponse.success(
                    UserOAuthAccountVO(
                        id = account.id,
                        platformId = account.platform,
                        nickname = account.nickname,
                        avatar = account.avatar,
                    )
                )
            }
            OAuthBindingScope.TENANT -> {
                val tenantId = userAuthentication.assertTenantIdNotNull()
                val bound = oAuthAccountService.bindTenant(account.id, userAuthentication.userId, tenantId)
                ApiResponse.success(
                    TenantOAuthAccountVO(
                        id = bound.id,
                        platformId = bound.platform,
                        scope = bound.scope,
                        tenantId = bound.tenantId,
                        nickname = bound.nickname,
                        avatar = bound.avatar,
                    )
                )
            }
        }
    }
}