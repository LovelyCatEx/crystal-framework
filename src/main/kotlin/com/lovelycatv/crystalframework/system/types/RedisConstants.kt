package com.lovelycatv.crystalframework.system.types

object RedisConstants {
    const val JWT_SIGN_KEY = "jwt_sign_key"

    const val ENTITY_CACHE_BY_ID = "entity-cache:id:"

    const val ENTITY_CACHE_BY_LIST = "entity-cache:list:"

    fun getRequestRegisterEmailCodeKey(email: String) = "register-email-code:$email"

    fun getRequestResetPasswordEmailCodeKey(email: String) = "reset-password-email-code:$email"

    fun getRequestResetEmailAddressEmailCodeKey(email: String) = "reset-email-address-email-code:$email"
}