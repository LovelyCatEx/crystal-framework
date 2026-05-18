package com.lovelycatv.crystalframework.encrypt.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EncryptedResponseData(
    val securityLevel: Int = 0,
    val disabled: Boolean = false,
)