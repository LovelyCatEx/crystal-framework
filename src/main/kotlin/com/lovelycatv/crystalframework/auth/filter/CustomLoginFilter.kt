/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.auth.filter

import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class CustomLoginFilter(
    defaultFilterProcessesUrl: String,
    authenticationManager: ReactiveAuthenticationManager,
    userAuthorizationService: UserAuthorizationService
) : AuthenticationWebFilter(authenticationManager) {

    init {
        setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, defaultFilterProcessesUrl)
        )

        setServerAuthenticationConverter { exchange ->
            exchange.formData
                .flatMap { params ->
                    val username = params["username"]?.firstOrNull()
                    val password = params["password"]?.firstOrNull()
                    val tenantId = params["tenantId"]?.firstOrNull() ?: 0

                    if (username == null || password == null) {
                        return@flatMap Mono.error(BusinessException("username or password is missing"))
                    }

                    UsernamePasswordAuthenticationToken(
                        "${username}:${tenantId}",
                        password,
                    ).toMono()
                }
        }

        setAuthenticationSuccessHandler { exchange, authentication ->
            val data = userAuthorizationService.buildLoginSuccessResponse(authentication.principal as UserEntity)

            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    ApiResponse.success(data).toJSONString().toByteArray()
                ).toMono()
            )
        }

        setAuthenticationFailureHandler { exchange, exception ->
            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    ApiResponse
                        .unauthorized<Nothing>(exception.localizedMessage)
                        .toJSONString()
                        .toByteArray()
                ).toMono()
            )
        }
    }
}