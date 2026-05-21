package com.lovelycatv.crystalframework.shared.exception

open class BusinessException(
    message: String = "",
    cause: Exception? = null
) : RuntimeException(message, cause)