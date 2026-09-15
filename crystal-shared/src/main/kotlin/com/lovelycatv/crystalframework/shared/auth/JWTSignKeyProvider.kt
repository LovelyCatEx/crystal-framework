/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
