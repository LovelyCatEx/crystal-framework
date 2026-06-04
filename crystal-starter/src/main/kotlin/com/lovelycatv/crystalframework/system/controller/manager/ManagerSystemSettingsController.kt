package com.lovelycatv.crystalframework.system.controller.manager

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.sdk.common.settings.buildSettingsSchemaResponse
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.controller.manager.dto.ManagerTestSendEmailDTO
import com.lovelycatv.crystalframework.system.controller.manager.dto.ManagerTestSendMessageDTO
import com.lovelycatv.crystalframework.system.controller.manager.vo.ManagerTestSendMessageResultVO
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/settings")
class ManagerSystemSettingsController(
    private val systemSettingsService: SystemSettingsService,
    private val systemSettingsRegistry: SystemSettingsRegistry,
    private val mailService: MailService,
    private val messageChannelService: MessageChannelService,
    private val jsonMapper: JsonMapper,
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_READ}')")
    @GetMapping("/schema")
    suspend fun getSystemSettings(): ApiResponse<*> {
        val data = buildSettingsSchemaResponse(systemSettingsRegistry.settingDeclarations()) { key ->
            systemSettingsService.getSettings(key)?.configValue
        }
        return ApiResponse.success(data)
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

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_MESSAGE}')")
    @PostMapping("/test-send-message")
    suspend fun testSendMessage(
        @RequestBody @Valid dto: ManagerTestSendMessageDTO,
    ): ApiResponse<ManagerTestSendMessageResultVO> {
        val channelType = ChannelType.fromTypeId(dto.channelType!!)
            ?: return ApiResponse.badRequest("Unknown channelType: ${dto.channelType}")

        val recipient: MessageRecipient = try {
            convertRecipient(channelType, dto.recipient!!)
        } catch (e: Exception) {
            return ApiResponse.badRequest("Invalid recipient for channel $channelType: ${e.message}")
        }

        val rawContent = dto.content?.takeIf { it.isNotBlank() }
            ?: defaultMessageFor(channelType)
        val message = ChainMessage(
            title = dto.title,
            chain = MessageChain.parse(rawContent),
        )

        val result = messageChannelService.send(recipient, message)
        val vo = ManagerTestSendMessageResultVO.from(result)
        return if (result.success) {
            ApiResponse.success(vo)
        } else {
            ApiResponse.internalServerError("Send failed: [${result.errorCode}] ${result.errorMessage}", vo)
        }
    }

    private fun convertRecipient(channelType: ChannelType, node: JsonNode): MessageRecipient = when (channelType) {
        ChannelType.EMAIL -> jsonMapper.treeToValue(node, EmailRecipient::class.java)
        ChannelType.LARK -> jsonMapper.treeToValue(node, LarkRecipient::class.java)
    }

    private fun defaultMessageFor(channelType: ChannelType): String {
        val testCfg = crystalFrameworkConfiguration.test
        return when (channelType) {
            ChannelType.LARK -> testCfg.messageChannel.lark.defaultMessage
            ChannelType.EMAIL -> testCfg.smtp.content
        }
    }
}