package com.lovelycatv.crystalframework.shared.exception

import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MissingRequestValueException
import org.springframework.web.server.ServerWebInputException

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
        logger.debug("An forbidden exception occurred", e)

        return ApiResponse.forbidden<Nothing>(
            e.localizedMessage ?: e.message ?: "you cannot access this resource")

    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(e: UnauthorizedException): ApiResponse<*> {
        logger.debug("An unauthorized exception occurred", e)

        return ApiResponse.unauthorized<Nothing>(
            e.localizedMessage ?: e.message ?: "you are not authorized to perform this action"
        )
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> {
        logger.debug("An business exception occurred", e)

        return ApiResponse.badRequest<Nothing>(e.localizedMessage ?: e.message ?: "bad request")
    }

    @ExceptionHandler(MissingRequestValueException::class)
    fun handleMissingRequestValueException(e: MissingRequestValueException): ApiResponse<*> {
        logger.debug("An exception occurred", e)

        return ApiResponse.badRequest<Nothing>("missing parameter ${e.name}")
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(e: WebExchangeBindException): ApiResponse<*> {
        logger.debug("An parameter validation exception occurred", e)

        return ApiResponse.badRequest<Nothing>(
            e.bindingResult.fieldErrors.joinToString(separator = ", ") {
                it.defaultMessage ?: "value of field ${it.field} cannot be ${it.rejectedValue?.toString()}"
            }
        )
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(e: AuthorizationDeniedException): ApiResponse<*> {
        logger.debug("An authorization denied exception occurred", e)

        return ApiResponse.forbidden<Nothing>("you are not allowed to access this resource")
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(e: ServerWebInputException): ApiResponse<*> {
        logger.debug("An parameter input exception occurred, message: ${e.localizedMessage ?: e.message}", e)

        return ApiResponse.badRequest<Nothing>(e.localizedMessage ?: e.message)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ApiResponse<*> {
        logger.debug("An duplicated key exception occurred, message: ${e.localizedMessage ?: e.message}", e)

        return ApiResponse.badRequest<Nothing>("duplicate resource id")
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ApiResponse<*> {
        logger.error("An unexpected exception occurred, message: ${e.localizedMessage ?: e.message}", e)

        return ApiResponse.internalServerError<Nothing>("internal server error")
    }
}