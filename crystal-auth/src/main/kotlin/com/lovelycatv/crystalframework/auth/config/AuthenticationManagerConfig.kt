package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.service.impl.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager

@Configuration
class AuthenticationManagerConfig(
    private val customUserDetailsService: CustomUserDetailsService
) {

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(customUserDetailsService)
    }
}
