/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.auth.filter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.crystalframework.rbac.types.PermissionType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.entity.UserEntity
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.kotlin.core.publisher.toMono

class CustomLoginFilter(
    defaultFilterProcessesUrl: String,
    authenticationManager: ReactiveAuthenticationManager
) : AuthenticationWebFilter(authenticationManager) {

    init {
        setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, defaultFilterProcessesUrl)
        )

        setServerAuthenticationConverter { exchange ->
           exchange.formData
                .handle { params, sink ->
                    val username = params["username"]?.firstOrNull()
                    val password = params["password"]?.firstOrNull()

                    if (username == null || password == null) {
                        sink.error(BusinessException("username or password is missing"))
                        return@handle
                    }

                    sink.next(UsernamePasswordAuthenticationToken(
                        username,
                        password,
                    ))
                }
        }

        setAuthenticationSuccessHandler { exchange, authentication ->
            val data = LoginSuccessResponseData().apply {
                token = JwtUtil.buildJwtToken(
                    signKey = "SpringBootTemplate",
                    authorities = authentication.authorities,
                    authentication = authentication,
                    expiration = 7 * 24 * 3600 * 1000L
                ) {
                    val principal = authentication.principal as UserEntity

                    this.claim("userId", principal.id.toString())
                }

                expiresIn = 7 * 24 * 3600 * 1000L
            }

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

    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
    )
    class LoginSuccessResponseData {
        @JsonProperty("token")
        var token: String? = null
        @JsonProperty("expiresIn")
        var expiresIn: Long = 0
    }
}