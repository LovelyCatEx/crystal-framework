package com.lovelycatv.crystalframework.auth.controller

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.service.AuditLogRecorder
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.auth.constants.LoginRateLimitConstants
import com.lovelycatv.crystalframework.auth.service.LoginRateLimitService
import com.lovelycatv.crystalframework.auth.service.OAuthBindingTokenService
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.crystalframework.shared.constants.TableConstants
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
import com.lovelycatv.crystalframework.shared.utils.resolveClientIp
import jakarta.validation.Valid
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/user/oauth")
class OAuthAccountController(
    private val userService: UserService,
    private val userAuthorizationService: UserAuthorizationService,
    private val oAuthAccountService: OAuthAccountService,
    private val oAuthBindingTokenService: OAuthBindingTokenService,
    private val auditLogRecorder: AuditLogRecorder,
    private val loginRateLimitService: LoginRateLimitService,
) {
    @Unauthorized
    @PostMapping("/bindOAuthAccount")
    suspend fun bindOAuthAccount(
        userAuthentication: UserAuthentication?,
        request: ServerHttpRequest,
        @ModelAttribute
        @Valid
        dto: BindOAuthAccountDTO
    ): ApiResponse<*> {
        val oauthAccountId = oAuthBindingTokenService.consume(dto.oauthBindToken)
        val targetUser = if (userAuthentication == null && dto.username == null && dto.password == null) {
            throw BusinessException("authentication and params cannot both be null")
        } else if (userAuthentication != null) {
            if (dto.username == null || dto.password == null) {
                userService.bindUserFromOAuthAccount(
                    oauthAccountId = oauthAccountId,
                    userId = userAuthentication.userId
                )
            } else {
                userService.bindUserFromOAuthAccount(
                    oauthAccountId = oauthAccountId,
                    username = dto.username,
                    password = dto.password,
                )
            }
        } else {
            // Unauthenticated credential verification path: rate-limit exactly like form login.
            val username = dto.username ?: throw BusinessException("unknown username")
            val password = dto.password ?: throw BusinessException("unknown password")
            val account = LoginRateLimitConstants.buildAccountKey(username)

            loginRateLimitService.checkAllowed(request.resolveClientIp(), account)

            val user = try {
                userService.bindUserFromOAuthAccount(
                    oauthAccountId = oauthAccountId,
                    username = username,
                    password = password,
                )
            } catch (e: Exception) {
                loginRateLimitService.recordFailure(account)
                throw e
            }
            loginRateLimitService.recordSuccess(account)
            user
        }

        if (userAuthentication != null) {
            auditLogRecorder.record(
                userAuthentication,
                AuditRequestContext.current(),
                AuditAction.UPDATE,
                TableConstants.TABLE_OAUTH_ACCOUNTS,
                listOf(oauthAccountId),
                true,
                null,
            )
        }

        return ApiResponse.success(
            userAuthorizationService.buildLoginSuccessResponse(targetUser)
        )
    }

    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_OAUTH_ACCOUNTS,
        resourceIds = "#dto.oauthAccountId",
    )
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
        val oauthAccountId = oAuthBindingTokenService.consume(dto.oauthBindToken)
        val user = userService.registerFromOAuthAccount(
            oauthAccountId = oauthAccountId,
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
        val oauthAccountId = oAuthBindingTokenService.consume(dto.oauthBindToken)
        val account = oAuthAccountService.getByIdOrNull(oauthAccountId)
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

        val response = when (scope) {
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

        auditLogRecorder.record(
            userAuthentication,
            AuditRequestContext.current(),
            AuditAction.UPDATE,
            TableConstants.TABLE_OAUTH_ACCOUNTS,
            listOf(account.id),
            true,
            null,
        )

        return response
    }
}