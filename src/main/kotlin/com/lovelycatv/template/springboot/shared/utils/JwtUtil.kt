/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.template.springboot.shared.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.util.*

object JwtUtil {
    fun buildJwtToken(
        signKey: String?,
        authorities: Iterable<GrantedAuthority>,
        authentication: Authentication,
        expiration: Long,
        customClaims: (JwtBuilder.() -> Unit)? = null
    ): String? {
        val authorityStr = StringBuilder()
        for (authority in authorities) {
            authorityStr.append(authority).append(",")
        }

        val builder = Jwts.builder()
            .claim("authorities", authorityStr)

        customClaims?.invoke(builder)

        builder
            .setSubject(authentication.name)
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, signKey)
            .compact()

        return builder.compact()
    }

    fun parseToken(signKey: String, token: String): Claims {
        return Jwts.parser()
            .setSigningKey(signKey)
            .parseClaimsJws(token.replace("Bearer ", ""))
            .getBody()
    }
}