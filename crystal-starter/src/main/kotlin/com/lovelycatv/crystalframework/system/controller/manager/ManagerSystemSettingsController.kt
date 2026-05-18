package com.lovelycatv.crystalframework.system.controller.manager

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import io.micrometer.observation.KeyValuesConvention
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/settings")
class ManagerSystemSettingsController(
    private val systemSettingsService: SystemSettingsService,
    private val keyValuesConvention: KeyValuesConvention
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_READ}')")
    @GetMapping("/schema")
    suspend fun getSystemSettings(): ApiResponse<*> {
        val declarations = SystemSettingsConstants.getAllDeclarations()

        val mapping = declarations.associate {
            it.key to mapOf(
                "sort" to it.sort,
                "valueType" to it.valueType.name,
                "value" to systemSettingsService.getSettings(it.key)?.configValue,
                "defaultValue" to it.defaultValue,
                "enumValues" to it.enumValues,
                "tab" to if (it.key.contains(".")) {
                    it.key.split(".")[0]
                } else {
                    null
                },
                "group" to if (it.key.contains(".")) {
                    it.key.split(".")
                        .dropLast(1)
                        .joinToString(".")
                } else {
                    null
                }
            )
        }

        return ApiResponse.success(
            mapOf(
                "groups" to mapping.values
                    .mapNotNull { it["group"] }
                    .distinct(),
                "items" to mapping,
            )
        )
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_UPDATE}')")
    @PostMapping("/update")
    suspend fun updateSystemSettings(
        @RequestBody dto: Map<String, String?>
    ): ApiResponse<*> {
        systemSettingsService.updateSystemSettings(dto)

        return ApiResponse.success(null)
    }
}