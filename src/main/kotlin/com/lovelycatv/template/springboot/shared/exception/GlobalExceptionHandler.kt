package com.lovelycatv.template.springboot.shared.exception

import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.MissingRequestValueException

@Component
@RestControllerAdvice
class GlobalExceptionHandler(private val auditEventRepository: AuditEventRepository) {
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

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(e: AuthorizationDeniedException): ApiResponse<*> {
        logger.info("An authorization denied exception occurred", e)

        return ApiResponse.forbidden<Nothing>("you are not allowed to access this resource")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ApiResponse<*> {
        logger.error("An unexpected exception occurred", e)

        return ApiResponse.internalServerError<Nothing>(e.localizedMessage ?: e.message ?: "")
    }
}