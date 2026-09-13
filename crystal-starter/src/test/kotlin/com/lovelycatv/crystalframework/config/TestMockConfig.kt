/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.user.service.EmailCodeAuthService
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestMockConfig {
    
    @Bean
    @Primary
    fun testEmailCodeAuthService(): EmailCodeAuthService {
        return mock()
    }
    
    @Bean
    @Primary
    fun testMailService(): MailService {
        return mock()
    }
}