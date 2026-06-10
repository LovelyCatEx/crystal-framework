package com.lovelycatv.crystalframework.messagechannel.channel.lark

import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap

/**
 * Thin wrapper around Lark's open-platform HTTP API. One bean per app; token cache keyed by
 * `(baseUrl, appId)`. The cache stores the absolute expiry timestamp and is refreshed in-place
 * whenever a request observes it within [TOKEN_REFRESH_LEEWAY_MS] of expiry.
 */
@Component
class LarkApiClient(
    private val objectMapper: ObjectMapper,
) {
    private val logger = logger()

    private val webClient: WebClient = WebClient.builder().build()

    private val tokenCache = ConcurrentHashMap<String, CachedToken>()

    suspend fun fetchTenantAccessToken(appId: String, appSecret: String, baseUrl: String): String {
        val cacheKey = "$baseUrl|$appId"
        val now = System.currentTimeMillis()

        tokenCache[cacheKey]?.let {
            if (it.expireAt - now > TOKEN_REFRESH_LEEWAY_MS) return it.token
        }

        val response = webClient.post()
            .uri("${baseUrl.removeSuffix("/")}$PATH_TENANT_TOKEN")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("app_id" to appId, "app_secret" to appSecret))
            .retrieve()
            .bodyToMono<Map<String, Any?>>()
            .awaitFirstOrNull()
            ?: throw IllegalStateException("Lark tenant_access_token endpoint returned empty body")

        val code = (response["code"] as? Number)?.toInt() ?: -1

        check(code == 0) {
            "Lark tenant_access_token failed: code=$code, msg=${response["msg"]}"
        }

        val token = response["tenant_access_token"] as? String
            ?: throw IllegalStateException("Lark response missing tenant_access_token")
        val expireSeconds = (response["expire"] as? Number)?.toLong() ?: DEFAULT_TOKEN_TTL_SECONDS

        tokenCache[cacheKey] = CachedToken(token, now + expireSeconds * 1000)
        return token
    }

    /**
     * Sends a message. [content] must already be a JSON-encoded string in the format expected
     * by Lark for [msgType] (e.g. `{"text":"hi"}`). Returns the lark message id on success.
     */
    suspend fun sendMessage(
        accessToken: String,
        baseUrl: String,
        receiveIdType: String,
        receiveId: String,
        msgType: String,
        content: String,
    ): LarkSendMessageResult {
        val body = mapOf(
            "receive_id" to receiveId,
            "msg_type" to msgType,
            "content" to content,
        )
        val response = webClient.post()
            .uri("${baseUrl.removeSuffix("/")}$PATH_SEND_MESSAGE?receive_id_type=$receiveIdType")
            .header("Authorization", "Bearer $accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(body))
            .retrieve()
            .bodyToMono<Map<String, Any?>>()
            .awaitFirstOrNull()
            ?: return LarkSendMessageResult(
                success = false,
                errorCode = "EMPTY_RESPONSE",
                errorMessage = "Lark send-message endpoint returned empty body",
            )

        val code = (response["code"] as? Number)?.toInt() ?: -1
        if (code != 0) {
            return LarkSendMessageResult(
                success = false,
                errorCode = code.toString(),
                errorMessage = response["msg"]?.toString(),
            )
        }

        @Suppress("UNCHECKED_CAST")
        val data = response["data"] as? Map<String, Any?>
        val messageId = data?.get("message_id") as? String
        return LarkSendMessageResult(success = true, messageId = messageId)
    }

    private data class CachedToken(val token: String, val expireAt: Long)

    private companion object {
        const val PATH_TENANT_TOKEN = "/open-apis/auth/v3/tenant_access_token/internal"
        const val PATH_SEND_MESSAGE = "/open-apis/im/v1/messages"
        const val TOKEN_REFRESH_LEEWAY_MS = 5 * 60_000L
        const val DEFAULT_TOKEN_TTL_SECONDS = 7200L
    }
}
