/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.template.springboot.auth.filter

import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.utils.JwtUtil
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import com.lovelycatv.vertex.log.logger
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.text.get

class CustomAuthFilter(
    val unauthorizedEndpoints: List<String>,
    val getUserAuthorities: (userId: Long) -> List<GrantedAuthority>
) : WebFilter {
    private val logger = logger()

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        if (exchange.request.path.toString() in unauthorizedEndpoints) {
            return chain.filter(exchange)
        }

        val authorization = exchange.request.headers["Authorization"]
            ?.firstOrNull()
            ?.replace("Bearer ", "")
            ?.trim()
            ?: throw BusinessException("Authorization header is missing")

        val claims = try {
            JwtUtil.parseToken("SpringBootTemplate", authorization)
        } catch (e: Exception) {
            if (e is ExpiredJwtException) {
                throw BusinessException("token expired")
            } else {
                logger.error("unexpected token parse exception", e)
                throw BusinessException("invalid token pattern")
            }
        }

        val userId = claims["userId"]
            ?.toString()
            ?.toLong()
            ?: throw BusinessException("userId is missing")

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