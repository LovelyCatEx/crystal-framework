package com.lovelycatv.crystalframework.system.controller.manager

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.controller.manager.dto.ManagerTestSendEmailDTO
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/settings")
class ManagerSystemSettingsController(
    private val systemSettingsService: SystemSettingsService,
    private val systemSettingsRegistry: SystemSettingsRegistry,
    private val mailService: MailService,
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_READ}')")
    @GetMapping("/schema")
    suspend fun getSystemSettings(): ApiResponse<*> {
        val declarations = systemSettingsRegistry.settingDeclarations()

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

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_EMAIL}')")
    @PostMapping("/test-send-email")
    suspend fun testSendEmail(
        @ModelAttribute @Valid dto: ManagerTestSendEmailDTO
    ): ApiResponse<*> {
        val testSmtp = crystalFrameworkConfiguration.test.smtp
        mailService.sendMail(
            to = dto.email!!,
            subject = testSmtp.subject,
            content = testSmtp.content
        )

        return ApiResponse.success(null)
    }
}