/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.template.springboot.auth.filter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.utils.JwtUtil
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
                .cache()
                .handle { params, sink ->
                    val username = params["username"]
                    val password = params["password"]

                    if (username == null || password == null) {
                        sink.error(BusinessException("Username or password is missing"))
                        return@handle
                    }

                    sink.next(UsernamePasswordAuthenticationToken(
                        username.toString().replace("[", "").replace("]", ""),
                        password.toString().replace("[", "").replace("]", ""),
                    ))
                }
        }

        setAuthenticationSuccessHandler { exchange, authentication ->
            println("${authentication.principal.toJSONString()}")
            val data = LoginSuccessResponseData().apply {
                token = JwtUtil.buildJwtToken(
                    signKey = "SpringBootTemplate",
                    authorities = authentication.authorities,
                    authentication = authentication,
                    expiration = 7 * 24 * 3600 * 1000L
                )
                expiresIn = 7 * 24 * 3600 * 1000L
            }

            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    data.toJSONString().toByteArray()
                ).toMono()
            )
        }

        setAuthenticationFailureHandler { exchange, exception ->
            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    ApiResponse.unauthorized<Nothing>(exception.localizedMessage).toJSONString().toByteArray()
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

    companion object {
        private val objectMapper = ObjectMapper()
    }
}