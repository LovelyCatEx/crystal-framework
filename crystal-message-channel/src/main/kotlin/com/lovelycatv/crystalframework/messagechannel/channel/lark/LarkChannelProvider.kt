package com.lovelycatv.crystalframework.messagechannel.channel.lark

import com.lovelycatv.crystalframework.messagechannel.channel.MessageChannelProvider
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.chain.ImageSegment
import com.lovelycatv.crystalframework.messagechannel.types.chain.MessageChain
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.LarkRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Sends messages to Lark recipients via Lark's IM v1 endpoint.
 *
 * App credentials are read from system settings on every call (cached internally by
 * [LarkApiClient] until expiry). When app credentials are blank, send returns a clear
 * `NOT_CONFIGURED` error so callers can surface it.
 *
 * Renderer selection (mirrors what users would expect from a single send entry-point):
 *   - chain contains an [ImageSegment] OR message has a non-blank title → `post` msg_type
 *   - otherwise → `text` msg_type
 */
@Component
class LarkChannelProvider(
    private val apiClient: LarkApiClient,
    private val textRenderer: ChainToLarkTextRenderer,
    private val postRenderer: ChainToLarkPostRenderer,
    private val systemModuleClient: SystemModuleClient,
    private val objectMapper: ObjectMapper,
    larkAtResolvers: List<LarkAtResolver>,
) : MessageChannelProvider {
    private val logger = logger()

    private val larkAtResolver: LarkAtResolver? = larkAtResolvers.firstOrNull()

    override val channelType: ChannelType = ChannelType.LARK

    override fun supports(recipient: MessageRecipient): Boolean = recipient is LarkRecipient

    override suspend fun send(
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        if (recipient !is LarkRecipient) {
            return SendResult.failed(
                channelType = channelType,
                errorCode = ERR_BAD_RECIPIENT,
                errorMessage = "LarkChannelProvider requires LarkRecipient but got ${recipient::class.simpleName}",
            )
        }

        val resolvedReceive = resolveReceiveId(recipient)
            ?: return SendResult.failed(
                channelType = channelType,
                errorCode = ERR_BAD_RECIPIENT,
                errorMessage = "LarkRecipient must carry at least one of openId/userId/unionId/email/chatId",
            )

        val larkConfig = systemModuleClient.getSystemSettings()?.messageChannel?.lark
            ?: return SendResult.failed(
                channelType = channelType,
                errorCode = ERR_SETTINGS_UNAVAILABLE,
                errorMessage = "System settings not initialized; cannot read Lark configuration",
            )
        if (larkConfig.appId.isBlank() || larkConfig.appSecret.isBlank()) {
            return SendResult.failed(
                channelType = channelType,
                errorCode = ERR_NOT_CONFIGURED,
                errorMessage = "Lark app credentials are not configured (messageChannel.lark.appId / appSecret)",
            )
        }

        return try {
            val token = apiClient.fetchTenantAccessToken(
                appId = larkConfig.appId,
                appSecret = larkConfig.appSecret,
                baseUrl = larkConfig.baseUrl,
            )
            val (msgType, content) = renderContent(message, larkConfig)

            val result = apiClient.sendMessage(
                accessToken = token,
                baseUrl = larkConfig.baseUrl,
                receiveIdType = resolvedReceive.idType,
                receiveId = resolvedReceive.id,
                msgType = msgType,
                content = content,
            )

            if (result.success) {
                SendResult.success(channelType, providerMessageId = result.messageId)
            } else {
                SendResult.failed(
                    channelType = channelType,
                    errorCode = result.errorCode ?: ERR_LARK_API,
                    errorMessage = result.errorMessage ?: "Lark API returned non-zero code",
                )
            }
        } catch (e: Exception) {
            logger.error("LarkChannelProvider send failed: {}", e.message, e)
            SendResult.failed(
                channelType = channelType,
                errorCode = ERR_LARK_API,
                errorMessage = e.message ?: "lark send failed",
            )
        }
    }

    private suspend fun renderContent(
        message: ChainMessage,
        @Suppress("UNUSED_PARAMETER") larkConfig: SystemSettings.MessageChannel.Lark,
    ): Pair<String, String> {
        val needsPost = !message.title.isNullOrBlank() || chainHasImage(message.chain)
        return if (needsPost) {
            val post = postRenderer.render(message.title, message.chain, larkAtResolver)
            MSG_TYPE_POST to objectMapper.writeValueAsString(post)
        } else {
            val text = textRenderer.render(message.chain, larkAtResolver)
            MSG_TYPE_TEXT to objectMapper.writeValueAsString(mapOf("text" to text))
        }
    }

    private fun chainHasImage(chain: MessageChain): Boolean = chain.segments.any { it is ImageSegment }

    private fun resolveReceiveId(recipient: LarkRecipient): ResolvedReceive? {
        recipient.openId?.takeIf { it.isNotBlank() }?.let { return ResolvedReceive(RECEIVE_ID_OPEN_ID, it) }
        recipient.userId?.takeIf { it.isNotBlank() }?.let { return ResolvedReceive(RECEIVE_ID_USER_ID, it) }
        recipient.unionId?.takeIf { it.isNotBlank() }?.let { return ResolvedReceive(RECEIVE_ID_UNION_ID, it) }
        recipient.email?.takeIf { it.isNotBlank() }?.let { return ResolvedReceive(RECEIVE_ID_EMAIL, it) }
        recipient.chatId?.takeIf { it.isNotBlank() }?.let { return ResolvedReceive(RECEIVE_ID_CHAT_ID, it) }
        return null
    }

    private data class ResolvedReceive(val idType: String, val id: String)

    private companion object {
        const val MSG_TYPE_TEXT = "text"
        const val MSG_TYPE_POST = "post"

        const val RECEIVE_ID_OPEN_ID = "open_id"
        const val RECEIVE_ID_USER_ID = "user_id"
        const val RECEIVE_ID_UNION_ID = "union_id"
        const val RECEIVE_ID_EMAIL = "email"
        const val RECEIVE_ID_CHAT_ID = "chat_id"

        const val ERR_BAD_RECIPIENT = "BAD_RECIPIENT"
        const val ERR_SETTINGS_UNAVAILABLE = "SETTINGS_UNAVAILABLE"
        const val ERR_NOT_CONFIGURED = "NOT_CONFIGURED"
        const val ERR_LARK_API = "LARK_API_ERROR"
    }
}
