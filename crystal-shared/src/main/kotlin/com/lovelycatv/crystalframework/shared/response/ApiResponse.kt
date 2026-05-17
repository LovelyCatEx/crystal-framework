package com.lovelycatv.crystalframework.shared.response

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    companion object {
        const val SUCCESS_CODE = 200
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val INTERNAL_SERVER_ERROR_CODE = 500

        fun <T> success(data: T?, message: String = "success") = ApiResponse(SUCCESS_CODE, message, data)

        fun <T> unauthorized(message: String, data: T? = null) = ApiResponse(UNAUTHORIZED, message, data)

        fun <T> forbidden(message: String, data: T? = null) = ApiResponse(FORBIDDEN, message, data)

        fun <T> badRequest(message: String, data: T? = null) = ApiResponse(BAD_REQUEST, message, data)

        fun <T> internalServerError(message: String, data: T? = null) = ApiResponse(INTERNAL_SERVER_ERROR_CODE, message, data)
    }
}