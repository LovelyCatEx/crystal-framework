package com.lovelycatv.crystalframework.shared.auth

/**
 * JWT sign key provider interface
 *
 * Provides the JWT signing key for token validation.
 * Implementations should handle key storage and retrieval.
 */
interface JWTSignKeyProvider {
    /**
     * Get the current JWT signing key
     *
     * @return JWT signing key
     */
    fun getSignKey(): String
}
