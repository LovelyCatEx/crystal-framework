package com.lovelycatv.template.springboot.user.config

import com.lovelycatv.template.springboot.user.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager

@Configuration
class AuthenticationManagerConfig(
    private val userService: UserService
) {

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userService)
    }
}