package com.lovelycatv.crystalframework.shared.constants

import java.time.Duration

object RedisConstants {
    const val JWT_SIGN_KEY = "jwt_sign_key"

    const val SYSTEM_INITIALIZE_TOKEN = "system-initialize-token"

    const val LOCK_SYSTEM_INITIALIZE = "lock:system:initialize"

    const val ENTITY_CACHE_BY_ID = "entity-cache:id:"

    const val ENTITY_CACHE_BY_LIST = "entity-cache:list:"

    /**
     * Lower TTL bound (ms) for cache entries rehydrated by a read-through miss in
     * [com.lovelycatv.crystalframework.shared.service.CachedBaseService.getByIdOrNull]. A read landing
     * inside a concurrent write's evict-around window can rehydrate the pre-write row; capping such
     * fills to a short TTL bounds how long that phantom can survive if the post-commit re-eviction
     * races it. Explicit writes via `updateCache` keep the full 8–12h TTL.
     */
    const val ENTITY_CACHE_READTHROUGH_MIN_TTL_MS = 30_000L

    /** Upper TTL bound (ms) for read-through miss rehydration. See [ENTITY_CACHE_READTHROUGH_MIN_TTL_MS]. */
    const val ENTITY_CACHE_READTHROUGH_MAX_TTL_MS = 120_000L

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

    const val OAUTH_BIND_TOKEN_PREFIX = "oauth:bind:token:"

    val OAUTH_BIND_TOKEN_TTL: Duration = Duration.ofMinutes(5)

    const val LOCK_APPROVAL_TOKEN_PREFIX = "lock:approval:token:"

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

    /** Prefix for per-user, per-tenant cached authorities: `userAuthorities:$userId:$tenantSlice`. */
    const val USER_AUTHORITIES_CACHE_PREFIX = "userAuthorities:"

    /** Prefix for the Set indexing every tenant-slice key of a user: `userAuthorities:index:$userId`. */
    const val USER_AUTHORITIES_INDEX_PREFIX = "userAuthorities:index:"

    /** Sentinel tenant slice used when a session carries no tenant (system scope). */
    const val NO_TENANT_SLICE = 0L

    /** TTL for a cached authority slice. */
    val USER_AUTHORITIES_CACHE_TTL: Duration = Duration.ofDays(3)

    /**
     * TTL for the per-user index Set. MUST stay strictly greater than [USER_AUTHORITIES_CACHE_TTL] and be
     * refreshed on every slice rebuild, so the index always outlives every slice it points to — otherwise
     * clearing the cache could miss live slices and leak stale cross-tenant authorities.
     */
    val USER_AUTHORITIES_INDEX_TTL: Duration = Duration.ofDays(4)

    fun getUserAuthoritiesCacheKey(userId: Long, tenantId: Long?) =
        "$USER_AUTHORITIES_CACHE_PREFIX$userId:${tenantId ?: NO_TENANT_SLICE}"

    fun getUserAuthoritiesIndexKey(userId: Long) = "$USER_AUTHORITIES_INDEX_PREFIX$userId"

    /** Prefix for the per-user force-logout timestamp (millis). */
    const val FORCE_LOGOUT_KEY_PREFIX = "forceLogout:"

    fun getForceLogoutKey(userId: Long) = "$FORCE_LOGOUT_KEY_PREFIX$userId"
}