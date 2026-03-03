package com.lovelycatv.crystalframework.shared.exception

class UnauthorizedException(
    message: String,
    cause: Exception? = null
) : RuntimeException(message, cause)