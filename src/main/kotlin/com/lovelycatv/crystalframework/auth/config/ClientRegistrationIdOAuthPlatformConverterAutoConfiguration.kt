package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.converters.types.ClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.auth.converters.DefaultClientRegistrationIdOAuthPlatformConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClientRegistrationIdOAuthPlatformConverterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ClientRegistrationIdOAuthPlatformConverter::class)
    fun clientRegistrationIdOAuthPlatformConverter(): ClientRegistrationIdOAuthPlatformConverter {
        return DefaultClientRegistrationIdOAuthPlatformConverter()
    }
}