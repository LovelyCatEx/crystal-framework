package com.lovelycatv.crystalframework.messagechannel.controller

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.controller.dto.SendTestMessageDTO
import com.lovelycatv.crystalframework.messagechannel.controller.vo.SendTestMessageResultVO
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

/**
 * Test endpoint for [MessageChannelService]. Given a [ChannelType], the controller converts the
 * raw recipient payload into the matching [MessageRecipient] subtype itself (no Jackson type
 * discriminator guessing), parses [content] from MessageChain XML, then dispatches.
 */
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/message-channel/test")
class MessageChannelTestController(
    private val messageChannelService: MessageChannelService,
    private val jsonMapper: JsonMapper,
) {
    @PreAuthorize("hasAuthority('${SystemPermission.ACTION_SYSTEM_SETTINGS_UPDATE}')")
    @PostMapping("/send")
    suspend fun send(
        @RequestBody @Valid dto: SendTestMessageDTO,
    ): ApiResponse<SendTestMessageResultVO> {
        val channelType = ChannelType.fromTypeId(dto.channelType!!)
            ?: return ApiResponse.badRequest("Unknown channelType: ${dto.channelType}")

        val recipient: MessageRecipient = try {
            convertRecipient(channelType, dto.recipient!!)
        } catch (e: Exception) {
            return ApiResponse.badRequest("Invalid recipient for channel $channelType: ${e.message}")
        }

        val message = ChainMessage(
            title = dto.title,
            chain = MessageChain.parse(dto.content!!),
        )

        val result = messageChannelService.send(recipient, message)
        val vo = SendTestMessageResultVO.from(result)
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
}
