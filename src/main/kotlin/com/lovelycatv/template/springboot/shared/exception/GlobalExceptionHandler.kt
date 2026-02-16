package com.lovelycatv.template.springboot.shared.exception

import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.MissingRequestValueException

@Component
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = logger()

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> {
        logger.info("An business exception occurred", e)

        return ApiResponse.badRequest<Nothing>(e.localizedMessage ?: e.message ?: "")
    }

    @ExceptionHandler(MissingRequestValueException::class)
    fun handleMissingRequestValueException(e: MissingRequestValueException): ApiResponse<*> {
        logger.info("An exception occurred", e)

        return ApiResponse.badRequest<Nothing>("missing parameter ${e.name}")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ApiResponse<*> {
        logger.error("An unexpected exception occurred", e)

        return ApiResponse.internalServerError<Nothing>(e.localizedMessage ?: e.message ?: "")
    }
}