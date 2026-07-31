package com.lovelycatv.crystalframework.shared.exception

class ForbiddenException(
    message: String = "",
    cause: Exception? = null,
    val context: ForbiddenContext? = null,
) : RuntimeException(message, cause)