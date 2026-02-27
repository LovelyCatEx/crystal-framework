package com.lovelycatv.crystalframework.system.controller.manager

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import io.micrometer.observation.KeyValuesConvention
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        val declarations = SystemSettingsConstants.getAllDeclarations().map { it.key }

        // Check whether the dto is fit to system settings declarations
        val test = dto.keys.all { it in declarations }
        if (!test) {
            throw BusinessException("Some keys in body is invalid")
        }

        dto.forEach { (key, value) ->
            systemSettingsService.setSettings(key, value)
        }

        return ApiResponse.success(null)
    }
}