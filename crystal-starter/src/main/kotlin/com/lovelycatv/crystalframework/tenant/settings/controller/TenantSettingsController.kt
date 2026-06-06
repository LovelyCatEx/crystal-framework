package com.lovelycatv.crystalframework.tenant.settings.controller

import com.lovelycatv.crystalframework.sdk.common.settings.buildSettingsSchemaResponse
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.settings.service.TenantSettingsService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant/settings")
class TenantSettingsController(
    private val tenantSettingsService: TenantSettingsService,
    private val tenantSettingsRegistry: TenantSettingsRegistry,
) {
    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_SETTINGS_READ_PEM}')")
    @GetMapping("/schema")
    suspend fun getTenantSettings(userAuthentication: UserAuthentication): ApiResponse<*> {
        val tenantId = userAuthentication.assertTenantIdNotNull()
        val data = buildSettingsSchemaResponse(tenantSettingsRegistry.settingDeclarations()) { key ->
            tenantSettingsService.getSettings(tenantId, key)?.configValue
        }
        return ApiResponse.success(data)
    }

    @PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_SETTINGS_UPDATE_PEM}')")
    @PostMapping("/update")
    suspend fun updateTenantSettings(
        userAuthentication: UserAuthentication,
        @RequestBody dto: Map<String, String?>,
    ): ApiResponse<*> {
        val tenantId = userAuthentication.assertTenantIdNotNull()
        tenantSettingsService.updateTenantSettings(tenantId, dto)
        return ApiResponse.success(null)
    }
}
