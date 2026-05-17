package com.lovelycatv.crystalframework.shared.exception

class ForbiddenException(
    message: String = "",
    cause: Exception? = null
) : RuntimeException(message, cause)