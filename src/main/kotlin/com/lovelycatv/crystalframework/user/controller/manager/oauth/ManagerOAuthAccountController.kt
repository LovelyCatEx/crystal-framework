package com.lovelycatv.crystalframework.user.controller.manager.oauth

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerCreateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerDeleteOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerReadOAuthAccountDTO
import com.lovelycatv.crystalframework.user.controller.manager.oauth.dto.ManagerUpdateOAuthAccountDTO
import com.lovelycatv.crystalframework.user.service.OAuthAccountManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/oauth-account")
class ManagerOAuthAccountController(
    private val oAuthAccountManagerService: OAuthAccountManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_OAUTH_ACCOUNT_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllOAuthAccounts(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(oAuthAccountManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_OAUTH_ACCOUNT_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateOAuthAccountDTO
    ): ApiResponse<*> {
        oAuthAccountManagerService.create(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_OAUTH_ACCOUNT_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadOAuthAccountDTO
    ): ApiResponse<*> {
        return ApiResponse.success(oAuthAccountManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_OAUTH_ACCOUNT_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateOAuthAccountDTO
    ): ApiResponse<*> {
        oAuthAccountManagerService.update(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_OAUTH_ACCOUNT_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteOAuthAccount(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteOAuthAccountDTO
    ): ApiResponse<*> {
        oAuthAccountManagerService.deleteByDTO(dto)

        return ApiResponse.success(null)
    }
}
