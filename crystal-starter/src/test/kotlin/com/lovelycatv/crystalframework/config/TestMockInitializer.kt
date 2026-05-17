package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.user.service.EmailCodeAuthService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.stereotype.Component

@Component
class TestMockInitializer(
    private val emailCodeAuthService: EmailCodeAuthService,
    private val mailService: MailService
) {
    
    @PostConstruct
    fun initMocks() {
        runBlocking {
            val emailCode = "123456"

            whenever(
                emailCodeAuthService.withSendEmailCode(
                    anyString(),
                    anyOrNull(),
                    anyOrNull()
                )
            ).thenAnswer { invocation ->
                val action = invocation.getArgument<suspend (String, MailService) -> Unit>(2)
                runBlocking { action(emailCode, mailService) }
            }

            whenever(mailService.sendMail(anyString(), anyString(), anyString())).thenAnswer { }
        }
    }
}