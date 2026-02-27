package com.lovelycatv.crystalframework.system.types

object RedisConstants {
    fun getRequestRegisterEmailCodeKey(email: String) = "register-email-code:$email"
    fun getRequestResetPasswordEmailCodeKey(email: String) = "reset-password-email-code:$email"
}