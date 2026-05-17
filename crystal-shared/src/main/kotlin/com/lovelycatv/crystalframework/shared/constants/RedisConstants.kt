package com.lovelycatv.crystalframework.shared.constants

object RedisConstants {
    const val JWT_SIGN_KEY = "jwt_sign_key"

    const val ENTITY_CACHE_BY_ID = "entity-cache:id:"

    const val ENTITY_CACHE_BY_LIST = "entity-cache:list:"

    const val SYSTEM_SETTINGS_REFRESH_TOPIC = "crystalframework:system-settings:refresh"

    const val SYSTEM_MAINTENANCE_TOPIC = "crystalframework:system-maintenance:refresh"

    const val SYSTEM_NORMALIZED_BASE_URL = "crystalframework:normalized-base-url"

    fun getRequestRegisterEmailCodeKey(email: String) = "register-email-code:$email"

    fun getRequestResetPasswordEmailCodeKey(email: String) = "reset-password-email-code:$email"

    fun getRequestResetEmailAddressEmailCodeKey(email: String) = "reset-email-address-email-code:$email"
}