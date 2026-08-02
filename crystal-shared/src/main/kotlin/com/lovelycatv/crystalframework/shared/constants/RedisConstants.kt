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

    /** Sliding-window counter of authentication attempts keyed by client IP. */
    const val LOGIN_RATE_LIMIT_IP_PREFIX = "auth:rl:ip:"

    /** Sliding-window counter of authentication attempts keyed by account (username:tenantId). */
    const val LOGIN_RATE_LIMIT_ACCOUNT_PREFIX = "auth:rl:acc:"

    /** Consecutive-failure counter per account, used to drive exponential-backoff lockout. */
    const val LOGIN_LOCK_FAILURE_PREFIX = "auth:lock:fail:"

    /** Lockout marker per account; value is the epoch-millis the lockout expires at. */
    const val LOGIN_LOCK_UNTIL_PREFIX = "auth:lock:until:"

    fun getLoginRateLimitIpKey(ip: String) = "$LOGIN_RATE_LIMIT_IP_PREFIX$ip"

    fun getLoginRateLimitAccountKey(account: String) = "$LOGIN_RATE_LIMIT_ACCOUNT_PREFIX$account"

    fun getLoginLockFailureKey(account: String) = "$LOGIN_LOCK_FAILURE_PREFIX$account"

    fun getLoginLockUntilKey(account: String) = "$LOGIN_LOCK_UNTIL_PREFIX$account"

    /** Sliding-window counter of email-code sends keyed by client IP. */
    const val MAIL_CODE_RATE_LIMIT_IP_PREFIX = "mail:rl:ip:"

    /** Sliding-window counter of email-code sends keyed by target email address. */
    const val MAIL_CODE_RATE_LIMIT_EMAIL_PREFIX = "mail:rl:email:"

    /** Single sliding-window counter of all email-code sends, guarding against spread-out mail bombing. */
    const val MAIL_CODE_RATE_LIMIT_GLOBAL_KEY = "mail:rl:global"

    fun getMailCodeRateLimitIpKey(ip: String) = "$MAIL_CODE_RATE_LIMIT_IP_PREFIX$ip"

    fun getMailCodeRateLimitEmailKey(email: String) = "$MAIL_CODE_RATE_LIMIT_EMAIL_PREFIX$email"
}