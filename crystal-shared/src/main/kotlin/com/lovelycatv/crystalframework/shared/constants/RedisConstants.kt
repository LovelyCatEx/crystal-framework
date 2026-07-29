package com.lovelycatv.crystalframework.shared.constants

object RedisConstants {
    const val JWT_SIGN_KEY = "jwt_sign_key"

    const val ENTITY_CACHE_BY_ID = "entity-cache:id:"

    const val ENTITY_CACHE_BY_LIST = "entity-cache:list:"

    const val SYSTEM_SETTINGS_REFRESH_TOPIC = "crystalframework:system-settings:refresh"

    const val TENANT_SETTINGS_REFRESH_TOPIC = "crystalframework:tenant-settings:refresh"

    const val SYSTEM_MAINTENANCE_TOPIC = "crystalframework:system-maintenance:refresh"

    const val JWT_SIGN_KEY_REFRESH_TOPIC = "crystalframework:jwt-sign-key:refresh"

    const val SYSTEM_SETTINGS = "system-settings"

    fun getTenantSettingsCacheKey(tenantId: Long) = "tenant-settings:$tenantId"

    object SpringSession {
        const val EXPIRATIONS = "spring:session:sessions:expirations"
    }

    fun getRequestRegisterEmailCodeKey(email: String) = "register-email-code:$email"

    fun getRequestResetPasswordEmailCodeKey(email: String) = "reset-password-email-code:$email"

    fun getRequestResetEmailAddressEmailCodeKey(email: String) = "reset-email-address-email-code:$email"

    const val LOCK_INVITATION_ACCEPT_PREFIX = "lock:invitation:accept:"

    const val LOCK_OAUTH_BIND_PREFIX = "lock:oauth:bind:"

    const val LOCK_APPROVAL_TASK_PREFIX = "lock:approval:task:"
}