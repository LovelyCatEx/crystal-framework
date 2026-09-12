package com.lovelycatv.crystalframework.shared.service.ratelimit

/**
 * Generic exponential-backoff lockout enforcer backed by Redis counters and TTL markers.
 *
 * Tracks consecutive failures per identity (account, device, etc.) and applies escalating lockout
 * durations once a threshold is crossed. Used by authentication entry points (login, WebSocket auth)
 * to defend against credential stuffing and brute-force attacks that operate just under the
 * sliding-window rate limit.
 *
 * Implementations must fail open: if the lockout infrastructure is unavailable, the request is
 * allowed rather than blocked, so a Redis outage never takes the protected feature offline.
 */
interface ExponentialBackoffLockout {
    /**
     * Throws [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException] if [identity]
     * is currently locked out. No-op when the lockout marker is absent or expired.
     *
     * @param keyPrefix Redis key prefix for this lockout dimension (e.g., "auth:lock:" or "ws:auth:lock:").
     * @param identity The locked identity (typically an account key like "username:tenantId").
     * @param message Human-readable message for the 429 response when rejected.
     */
    suspend fun checkLockout(keyPrefix: String, identity: String, message: String)

    /**
     * Increments the consecutive-failure counter for [identity] and, once [threshold] is crossed,
     * sets an exponential-backoff lockout (`baseSeconds * 2^(failures - threshold)`, capped at [maxSeconds]).
     *
     * @param keyPrefix Redis key prefix for this lockout dimension.
     * @param identity The failing identity.
     * @param threshold Number of consecutive failures before lockout starts.
     * @param baseSeconds Base lockout duration (first lockout = baseSeconds, second = baseSeconds * 2, etc.).
     * @param maxSeconds Maximum lockout duration (prevents unbounded escalation).
     * @param windowSeconds Failure counter TTL (should be >= maxSeconds so escalation state survives the lockout).
     */
    suspend fun recordFailure(
        keyPrefix: String,
        identity: String,
        threshold: Int,
        baseSeconds: Int,
        maxSeconds: Int,
        windowSeconds: Int,
    )

    /**
     * Clears the failure counter and lockout marker for [identity] (typically called on successful auth).
     */
    suspend fun clearLockout(keyPrefix: String, identity: String)
}
