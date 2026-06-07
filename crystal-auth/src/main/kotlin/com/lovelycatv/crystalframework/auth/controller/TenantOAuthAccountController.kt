package com.lovelycatv.crystalframework.auth.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants.REQUEST_MAPPING_PREFIX
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.auth.controller.dto.BindTenantOAuthAccountDTO
import com.lovelycatv.crystalframework.auth.controller.dto.UnbindTenantOAuthAccountDTO
import com.lovelycatv.crystalframework.auth.controller.vo.TenantOAuthAccountVO
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Tenant-scoped OAuth bindings for the currently authenticated member. The acting tenant is
 * taken from the authentication (the member must already be logged into that tenant), so a
 * member can only manage bindings within their own tenant — tenant admins cannot reach across.
 */
@Validated
@RestController
@RequestMapping("$REQUEST_MAPPING_PREFIX/tenant/oauth")
class TenantOAuthAccountController(
    private val oAuthAccountService: OAuthAccountService,
) {
    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_OAUTH_READ_PEM}')")
    @GetMapping("/accounts")
    suspend fun getTenantOAuthAccounts(
        userAuthentication: UserAuthentication,
    ): ApiResponse<*> {
        val tenantId = userAuthentication.assertTenantIdNotNull()

        return ApiResponse.success(
            oAuthAccountService
                .getUserTenantOAuthAccounts(userAuthentication.userId, tenantId)
                .map {
                    TenantOAuthAccountVO(
                        id = it.id,
                        platformId = it.platform,
                        scope = it.scope,
                        tenantId = it.tenantId,
                        nickname = it.nickname,
                        avatar = it.avatar,
                    )
                }
        )
    }

    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_OAUTH_BIND_PEM}')")
    @PostMapping("/bind")
    suspend fun bindTenantOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: BindTenantOAuthAccountDTO
    ): ApiResponse<*> {
        val tenantId = userAuthentication.assertTenantIdNotNull()

        val bound = oAuthAccountService.bindTenant(
            accountId = dto.oauthAccountId,
            userId = userAuthentication.userId,
            tenantId = tenantId,
        )

        return ApiResponse.success(
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

    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_OAUTH_UNBIND_PEM}')")
    @PostMapping("/unbind")
    suspend fun unbindTenantOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UnbindTenantOAuthAccountDTO
    ): ApiResponse<*> {
        val tenantId = userAuthentication.assertTenantIdNotNull()

        oAuthAccountService.unbindTenant(
            accountId = dto.oauthAccountId,
            userId = userAuthentication.userId,
            tenantId = tenantId,
        )

        return ApiResponse.success(true)
    }
}
