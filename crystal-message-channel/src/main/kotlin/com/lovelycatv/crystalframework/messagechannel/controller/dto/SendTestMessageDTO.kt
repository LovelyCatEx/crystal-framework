package com.lovelycatv.crystalframework.messagechannel.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import tools.jackson.databind.JsonNode

/**
 * Request body for the message-channel test endpoint.
 *
 * [recipient] is kept as a raw [JsonNode] and converted by the controller into the concrete
 * [com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient] subtype
 * selected by [channelType] — the addressing fields differ per channel:
 * ```json
 * { "channelType": 1, "recipient": { "email": "a@b.com", "displayName": "Bob" },
 *   "title": "hi", "content": "hello <at user=\"1001\"/><br/>world" }
 * { "channelType": 2, "recipient": { "openId": "ou_xxx" },
 *   "content": "<link href=\"https://x.y\" title=\"open\"/>" }
 * ```
 *
 * @param channelType target channel, value of [com.lovelycatv.crystalframework.messagechannel.constants.ChannelType.typeId].
 * @param recipient channel-specific addressing payload, deserialized per [channelType].
 * @param title optional message title — used as email subject, ignored by plain-text channels.
 * @param content message body in [com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain] XML form.
 */
data class SendTestMessageDTO(
    @field:NotNull(message = "channelType must not be null")
    val channelType: Int?,

    @field:NotNull(message = "recipient must not be null")
    val recipient: JsonNode?,

    val title: String? = null,

    @field:NotBlank(message = "content must not be blank")
    val content: String?,
)
