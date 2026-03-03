/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.auth.filter

import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.vertex.log.logger
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import reactor.core.publisher.Mono

class CustomAuthFilter(
    val unauthorizedPathPatterns: List<PathPattern>,
    val getUserAuthorities: (userId: Long) -> List<GrantedAuthority>,
    val getJWTSignKey: () -> String,
) : WebFilter {
    private val logger = logger()

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val requestPath = exchange.request.path.pathWithinApplication()

        // Check if the path matches any unauthorized endpoint pattern
        val isUnauthorized = unauthorizedPathPatterns.any { pattern ->
            pattern.matches(requestPath)
        }

        if (isUnauthorized) {
            return chain.filter(exchange)
        }

        val authorization = exchange.request.headers["Authorization"]
            ?.firstOrNull()
            ?.replace("Bearer ", "")
            ?.trim()
            ?: throw UnauthorizedException("Authorization header is missing")

        val claims = try {
            JwtUtil.parseToken(getJWTSignKey.invoke(), authorization)
        } catch (e: Exception) {
            if (e is ExpiredJwtException) {
                throw UnauthorizedException("token expired")
            } else {
                logger.error("unexpected token parse exception", e)
                throw UnauthorizedException("invalid token pattern")
            }
        }

        val userId = claims["userId"]
            ?.toString()
            ?.toLong()
            ?: throw UnauthorizedException("userId is missing")

        val token = UsernamePasswordAuthenticationToken(
            claims.subject,
            null,
            getUserAuthorities.invoke(userId)
        )

        return chain.filter(exchange).contextWrite {
            ReactiveSecurityContextHolder.withAuthentication(token)
        }
    }
}