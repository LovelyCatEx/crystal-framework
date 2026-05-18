package com.lovelycatv.crystalframework.filter

import com.lovelycatv.crystalframework.shared.constants.HeadersConstants
import com.lovelycatv.crystalframework.shared.constants.SessionConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.SystemSettings
import com.lovelycatv.crystalframework.shared.utils.encrypt.AES
import com.lovelycatv.crystalframework.shared.utils.encrypt.RSA
import com.lovelycatv.vertex.log.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.HandlerResultHandler
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import tools.jackson.databind.ObjectMapper

/**
 * Hybrid RSA+AES encryption for API responses.
 *
 * Flow:
 * 1. Frontend sends RSA public key via X-Secure-Key header
 * 2. Backend generates AES key, encrypts it with RSA public key, returns via X-Secure-AES-Key header
 * 3. Backend encrypts response body with AES key
 * 4. Subsequent requests: backend reuses AES key from session, frontend already has it
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class EncryptResponseAdvice(
    private val codecConfigurer: ServerCodecConfigurer,
    private val resolver: RequestedContentTypeResolver,
    private val redisService: ReactiveRedisService,
    private val objectMapper: ObjectMapper
) : HandlerResultHandler {
    private val logger = logger()

    private val delegate: ResponseBodyResultHandler by lazy {
        ResponseBodyResultHandler(codecConfigurer.writers, resolver)
    }

    override fun supports(result: HandlerResult): Boolean {
        return delegate.supports(result)
    }

    override fun handleResult(
        exchange: ServerWebExchange,
        result: HandlerResult
    ): Mono<Void> {
        @Suppress("UNCHECKED_CAST")
        val originalBody = result.returnValue
        if (originalBody == null) {
            val newResult = HandlerResult(
                result.handler,
                originalBody,
                result.returnTypeSource
            )
            return delegate.handleResult(exchange, newResult)
        }

        val systemSettings = SystemSettings(
            basic = SystemSettings.Basic(baseUrl = ""),
            bootstrap = SystemSettings.Bootstrap(autoCheckRbacTableData = true),
            mail = SystemSettings.Mail(
                smtp = SystemSettings.Mail.SMTP(
                    host = "", port = 0, username = "", password = "", ssl = true, fromEmail = ""
                )
            ),
            security = SystemSettings.Security(
                api = SystemSettings.Security.Api(
                    encrypt = SystemSettings.Security.Api.Encrypt(enabled = true)
                )
            )
        ).toMono()

        val sessionMono = exchange.session

        val modifiedBody = systemSettings
            .flatMap { settings ->
                if (!settings.security.api.encrypt.enabled) {
                    return@flatMap (originalBody as? Mono<*> ?: originalBody.toMono())
                }

                sessionMono.flatMap { session ->
                    // Try to get existing AES key from session
                    var aesKey = session.getAttribute<String>(SessionConstants.API_ENCRYPT_AES_KEY)

                    if (aesKey == null) {
                        // No AES key yet — need RSA public key to establish one
                        val rsaPublicKey = exchange.request.headers.get(HeadersConstants.X_SECURE_KEY)?.firstOrNull()
                            ?: throw BusinessException("The server is protected by asymmetric encryption. Provide X-Secure-Key header.")

                        // Generate new AES key
                        aesKey = AES.generateSecretKey()

                        // Store AES key in session
                        session.attributes[SessionConstants.API_ENCRYPT_AES_KEY] = aesKey
                        session.attributes[SessionConstants.API_ENCRYPT_RSA_PUB_KEY] = rsaPublicKey

                        // Encrypt AES key with RSA public key and send via response header
                        val encryptedAesKey = RSA.encryptWithPublicKey(aesKey, rsaPublicKey)
                        exchange.response.headers.set(HeadersConstants.X_SECURE_AES_KEY, encryptedAesKey)
                    }

                    // Encrypt response body with AES
                    processWithAes(originalBody, aesKey)
                }
            }

        val newResult = HandlerResult(
            result.handler,
            modifiedBody,
            result.returnTypeSource
        )

        return delegate.handleResult(exchange, newResult)
    }

    private fun processWithAes(originalBody: Any, aesKey: String): Mono<Any> {
        return if (originalBody is Mono<*>) {
            originalBody.map { responseBody ->
                encryptResponseBody(responseBody, aesKey)
            }
        } else {
            encryptResponseBody(originalBody, aesKey).toMono()
        }
    }

    private fun encryptResponseBody(responseBody: Any, aesKey: String): Any {
        return if (responseBody is ApiResponse<*>) {
            val json = objectMapper.writeValueAsString(responseBody)
            ApiResponse(
                responseBody.code,
                responseBody.message,
                AES.encryptWithAES(json, aesKey)
            )
        } else {
            logger.warn("Response body is not ApiResponse, skipping encryption")
            responseBody
        }
    }
}
