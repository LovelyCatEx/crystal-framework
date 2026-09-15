/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.user.converters.DefaultClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.user.converters.types.ClientRegistrationIdOAuthPlatformConverter
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