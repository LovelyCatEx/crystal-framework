/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode

@Configuration(proxyBeanMethods = false)
class SnowIdGeneratorConfig {
    @Bean
    @Scope(proxyMode = ScopedProxyMode.NO)
    fun snowIdGenerator(): SnowIdGenerator {
        return SnowIdGenerator(
            0,
            41,
            5,
            5,
            12,
            0,
            0,
            0,
            0
        )
    }
}