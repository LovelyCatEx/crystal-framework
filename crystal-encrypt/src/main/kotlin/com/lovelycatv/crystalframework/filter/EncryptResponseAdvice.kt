package com.lovelycatv.crystalframework.filter

import com.lovelycatv.crystalframework.shared.constants.HeadersConstants
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.constants.SessionConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.SystemSettings
import com.lovelycatv.crystalframework.shared.utils.encrypt.RSA
import com.lovelycatv.vertex.log.logger
import org.checkerframework.checker.units.qual.m
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.HandlerResultHandler
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import tools.jackson.databind.ObjectMapper

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class EncryptResponseAdvice(
    private val codecConfigurer: ServerCodecConfigurer,
    private val resolver: RequestedContentTypeResolver,
    private val sessionRepository: ReactiveRedisIndexedSessionRepository,
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

        // val systemSettings = redisService.get<SystemSettings>(RedisConstants.SYSTEM_SETTINGS)
        val systemSettings = SystemSettings(
            basic = SystemSettings.Basic(
                baseUrl = "",
            ),
            bootstrap = SystemSettings.Bootstrap(
                autoCheckRbacTableData = true
            ),
            mail = SystemSettings.Mail(
                smtp = SystemSettings.Mail.SMTP(
                    host = "",
                    port = 0,
                    username = "",
                    password = "",
                    ssl = true,
                    fromEmail = ""
                )
            ),
            security = SystemSettings.Security(
                api = SystemSettings.Security.Api(
                    encrypt = SystemSettings.Security.Api.Encrypt(
                        enabled = true
                    )
                )
            )
        ).toMono()

        val sessionMono = exchange.session

        val modifiedBody = systemSettings
            .flatMap { systemSettings ->
                if (systemSettings.security.api.encrypt.enabled) {
                    // Api encryption enabled
                    sessionMono.flatMap { session ->
                        val publicKey = session.getAttribute<String>(SessionConstants.API_ENCRYPT_RSA_PUB_KEY)

                        if (publicKey != null) {
                            process(originalBody, publicKey)
                        } else {
                            // Get public key from header
                            val publicKeyFromHeader = exchange.request.headers.get(HeadersConstants.X_SECURE_KEY)?.get(0)
                            if (publicKeyFromHeader != null) {
                                // Save into session
                                session.attributes[SessionConstants.API_ENCRYPT_RSA_PUB_KEY] = publicKeyFromHeader

                                process(originalBody, publicKey)
                            } else {
                                throw BusinessException("The server is protected by asymmetric encryption")
                            }
                        }

                    }
                } else {
                    // Do nothing
                    originalBody as? Mono<*> ?: originalBody.toMono()
                }
            }

        val newResult = HandlerResult(
            result.handler,
            modifiedBody,
            result.returnTypeSource
        )

        return delegate.handleResult(exchange, newResult)
    }

    private fun process(originalBody: Any, publicKey: String?): Mono<Any> {
        require(publicKey != null && validatePublicKey(publicKey)) {
            "Public key does not match required parameters"
        }

        return if (originalBody is Mono<*>) {
            originalBody.map { responseBody ->
                processPlain(responseBody, publicKey)
            }
        } else {
            processPlain(originalBody, publicKey).toMono()
        }
    }

    private fun processPlain(responseBody: Any, publicKey: String): Any {
        require(validatePublicKey(publicKey)) {
            "Public key does not match required parameters"
        }

        return if (responseBody is ApiResponse<*>) {
            ApiResponse(
                responseBody.code,
                responseBody.message,
                RSA.encryptWithPublicKey(
                    objectMapper.writeValueAsString(responseBody),
                    publicKey
                )
            )
        } else {
            logger.warn("As the type of response body is not ${ApiResponse::class.qualifiedName}, the encryption will be skipped")
            responseBody
        }
    }

    private fun validatePublicKey(publicKey: String?): Boolean {
        return !publicKey.isNullOrBlank()
    }
}