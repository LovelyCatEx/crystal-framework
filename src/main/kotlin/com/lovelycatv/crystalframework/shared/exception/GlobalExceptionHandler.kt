package com.lovelycatv.crystalframework.shared.exception

import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MissingRequestValueException

@Component
@RestControllerAdvice
class GlobalExceptionHandler(private val auditEventRepository: AuditEventRepository) {
    private val logger = logger()

    fun handle(e: Exception): ApiResponse<*> {
        return when (e) {
            is ForbiddenException -> {
                this.handleForbiddenException(e)
            }

            is UnauthorizedException -> {
                this.handleUnauthorizedException(e)
            }

            is BusinessException -> {
                this.handleBusinessException(e)
            }

            is MissingRequestValueException -> {
                this.handleMissingRequestValueException(e)
            }

            is AuthorizationDeniedException -> {
                this.handleAuthorizationDeniedException(e)
            }

            is WebExchangeBindException -> {
                this.handleWebExchangeBindException(e)
            }

            else -> {
                this.handleException(e)
            }
        }
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ApiResponse<*> {
        logger.info("An forbidden exception occurred", e)

        return ApiResponse.forbidden<Nothing>(e.localizedMessage ?: e.message ?: "")
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(e: UnauthorizedException): ApiResponse<*> {
        logger.info("An unauthorized exception occurred", e)

        return ApiResponse.unauthorized<Nothing>(e.localizedMessage ?: e.message ?: "")
    }

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

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(e: WebExchangeBindException): ApiResponse<*> {
        logger.info("An parameter validation exception occurred", e)

        return ApiResponse.badRequest<Nothing>(
            e.bindingResult.fieldErrors.joinToString(separator = ", ") {
                it.defaultMessage ?: "value of field ${it.field} cannot be ${it.rejectedValue?.toString()}"
            }
        )
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