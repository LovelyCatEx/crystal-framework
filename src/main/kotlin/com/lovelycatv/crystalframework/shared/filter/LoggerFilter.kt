package com.lovelycatv.crystalframework.shared.filter

import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.log.logger
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class LoggerFilter(private val snowIdGenerator: SnowIdGenerator) : WebFilter {
    private val logger = logger()

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val originalRequest = exchange.request
        val originalResponse = exchange.response

        val id = snowIdGenerator.nextId()

        val decoratedRequest = RequestDecorator(id, originalRequest, originalResponse, logger)
        val decoratedResponse = ResponseDecorator(id, originalRequest, originalResponse, logger)

        return chain.filter(
            exchange.mutate()
                .request(decoratedRequest)
                .response(decoratedResponse)
                .build()
        )
    }

    class RequestDecorator(
        private val id: Long,
        private val originalRequest: ServerHttpRequest,
        private val response: ServerHttpResponse,
        private val logger: Logger
    ) : ServerHttpRequestDecorator(originalRequest) {
        override fun getBody(): Flux<DataBuffer> {
            return super.getBody().collectList().flatMapMany { dataBuffers ->
                val joined = dataBuffers.joinToString("") { buffer ->
                    val bytes = ByteArray(buffer.readableByteCount())
                    buffer.read(bytes)
                    DataBufferUtils.release(buffer)
                    String(bytes, StandardCharsets.UTF_8)
                }

                logger.info("[$id] Request Body:")
                if (joined.isNotEmpty()) {
                    if (joined.length > 2000) {
                        logger.info("[$id]   ${joined.substring(0, 2000)}... (truncated)")
                    } else {
                        logger.info("[$id]   $joined")
                    }
                } else {
                    logger.info("[$id]   (empty)")
                }

                Flux.just(
                    response
                        .bufferFactory()
                        .wrap(joined.toByteArray(StandardCharsets.UTF_8))
                )
            }
        }
    }

    class ResponseDecorator(
        private val id: Long,
        private val originalRequest: ServerHttpRequest,
        response: ServerHttpResponse,
        private val logger: Logger
    ) : ServerHttpResponseDecorator(response) {
        override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
            logger.info("[$id] ${originalRequest.method.name()} ${originalRequest.uri}")
            logger.info("[$id] RemoteIpAddress: ${originalRequest.remoteAddress?.toString() ?: "Unknown"}")

            // Request Headers
            logger.info("[$id] Request Headers:")
            originalRequest.headers.forEach { headerName, values ->
                logger.info("[$id]   - $headerName = ${values.joinToString(separator = ", ")}")
            }

            // Response Headers
            logger.info("[$id] Response Headers:")
            delegate.headers.forEach { headerName, values ->
                logger.info("[$id]   - $headerName = ${values.joinToString(separator = ", ")}")
            }

            val bufferProcessor = { buffer: DataBuffer ->
                val content = ByteArray(buffer.readableByteCount())
                buffer.read(content)
                DataBufferUtils.release(buffer)

                val bodyStr = String(content, StandardCharsets.UTF_8)

                logger.info("[$id] Response Body:")
                printResponseBodyString(id, bodyStr)
                logger.info("=".repeat(96))

                content
            }

            return if (body is Flux<*>) {
                val flux = body as Flux<out DataBuffer>
                super.writeWith(
                    flux.map { buffer ->
                        delegate.bufferFactory().wrap(bufferProcessor.invoke(buffer))
                    }
                )
            } else {
                DataBufferUtils.join(Flux.from(body))
                    .flatMap { dataBuffer ->
                        val newBuffer = delegate
                            .bufferFactory()
                            .wrap(bufferProcessor.invoke(dataBuffer))

                        super.writeWith(Mono.just(newBuffer))
                    }
            }
        }

        override fun writeAndFlushWith(
            body: Publisher<out Publisher<out DataBuffer>>
        ): Mono<Void> {
            return super.writeAndFlushWith(body)
        }

        private fun printResponseBodyString(id: Long, bodyStr: String) {
            if (bodyStr.isNotEmpty()) {
                logger.info("[$id]   $bodyStr")
            } else {
                logger.info("[$id]   (empty)")
            }
        }
    }
}