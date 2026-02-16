package com.lovelycatv.template.springboot.shared.exception

import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.webflux.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {
    private val logger = logger()

    init {
        setMessageWriters(applicationContext.getBean<ServerCodecConfigurer>().writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request ->
            val error = getError(request)
            val errorProperties = getErrorAttributes(request, ErrorAttributeOptions.defaults())

            when (error) {
                is BusinessException -> {
                    ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.badRequest(
                            error.localizedMessage ?: error.message ?: "",
                            HttpStatus.BAD_REQUEST.value()
                        ))
                }
                else -> {
                    logger.error("An unexpected error occurred", error)

                    ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(
                            ApiResponse.internalServerError<Nothing>(
                            errorProperties["message"] as? String ?: "Unknown error"
                        ))
                }
            }
        }
    }
}