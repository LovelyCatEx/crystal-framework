package com.lovelycatv.crystalframework.rbac.user.service

interface UserForceLogoutService {
    /**
     * Marks a user as force-logged-out at the current moment. Any JWT whose `issuedAt` is earlier
     * than the stored timestamp will be rejected by the auth filter until the record naturally
     * expires (TTL = JWT validity), by which point those tokens are expired anyway.
     */
    suspend fun markForceLogout(userId: Long)

    /**
     * Returns the epoch-millis at which the user was force-logged-out, or null if no active record.
     */
    suspend fun getForceLogoutAt(userId: Long): Long?
}
