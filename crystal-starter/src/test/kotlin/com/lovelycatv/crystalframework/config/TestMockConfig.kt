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