package com.lovelycatv.crystalframework.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordEncoderConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val encoders = mapOf(
            "bcrypt" to BCryptPasswordEncoder()
        )

        val delegatingEncoder = DelegatingPasswordEncoder("bcrypt", encoders)

        delegatingEncoder.setDefaultPasswordEncoderForMatches(BCryptPasswordEncoder())

        return delegatingEncoder
    }
}