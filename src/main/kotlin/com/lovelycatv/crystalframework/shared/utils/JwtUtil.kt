/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.shared.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import java.security.MessageDigest
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object JwtUtil {
    /**
     * Derive a SecretKey from a raw [signKey] string.
     *
     * HS512 requires at least 64-byte keys. We always feed the raw bytes through
     * SHA-512 so that any user provided string (e.g. UUID) yields a 64-byte key
     * suitable for [io.jsonwebtoken.Jwts.SIG.HS512].
     */
    private fun deriveSecretKey(signKey: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-512").digest(signKey.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(digest, "HmacSHA512")
    }

    fun buildJwtToken(
        signKey: String,
        subject: String,
        authorities: Set<String>,
        expiration: Long,
        customClaims: (JwtBuilder.() -> Unit)? = null
    ): String {
        val authorityStr = authorities.joinToString(",")

        val builder = Jwts
            .builder()
            .claim("authorities", authorityStr)

        customClaims?.invoke(builder)

        return builder
            .subject(subject)
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(deriveSecretKey(signKey), Jwts.SIG.HS512)
            .compact()
    }

    fun parseToken(signKey: String, token: String): Claims {
        return Jwts.parser()
            .verifyWith(deriveSecretKey(signKey))
            .build()
            .parseSignedClaims(token.removePrefix("Bearer ").trim())
            .payload
    }
}
