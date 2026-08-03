package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.system.service.impl.SystemInitializeServiceImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SystemInitializeServiceTest {
    @Test
    fun startupCreatesTokenOnlyWhenRedisKeyIsAbsent() = runTest {
        val redisService = mock<ReactiveRedisService> {
            on { compareAndDelete(any(), any()) } doReturn Mono.just(true)
        }
        whenever(redisService.setIfAbsent<String>(any(), any(), anyOrNull()))
            .thenReturn(Mono.just(true), Mono.just(true))
        val transactionService = mock<SystemInitializeTransactionService> {
            onBlocking { isSystemInitialized() } doReturn false
        }
        val service = SystemInitializeServiceImpl(redisService, transactionService)

        val token = service.prepareInitializationToken()

        assertEquals(43, token?.length)
        verify(redisService).setIfAbsent(RedisConstants.SYSTEM_INITIALIZE_TOKEN, token!!)
    }

    @Test
    fun startupPreservesExistingTokenWithoutReturningIt() = runTest {
        val redisService = mock<ReactiveRedisService> {
            on { compareAndDelete(any(), any()) } doReturn Mono.just(true)
        }
        whenever(redisService.setIfAbsent<String>(any(), any(), anyOrNull()))
            .thenReturn(Mono.just(true), Mono.just(false))
        val transactionService = mock<SystemInitializeTransactionService> {
            onBlocking { isSystemInitialized() } doReturn false
        }
        val service = SystemInitializeServiceImpl(redisService, transactionService)

        assertEquals(null, service.prepareInitializationToken())
        verify(redisService, never()).get<String>(RedisConstants.SYSTEM_INITIALIZE_TOKEN)
    }

    @Test
    fun invalidTokenIsRejectedWithoutInitializationOrConsumption() = runTest {
        val redisService = redisServiceWithToken("expected")
        val transactionService = mock<SystemInitializeTransactionService> {
            onBlocking { isSystemInitialized() } doReturn false
        }
        val service = SystemInitializeServiceImpl(redisService, transactionService)

        val exception = assertFailsWith<ForbiddenException> {
            initialize(service, "invalid")
        }

        assertEquals(ForbiddenReason.INVALID_INITIALIZATION_TOKEN, exception.context?.reason)
        assertEquals(ResourceScope.SYSTEM, exception.context?.scope)
        verify(transactionService, never()).initializeSystem(any(), any(), any(), any(), any(), any(), any(), any(), any())
        verify(redisService, never()).compareAndDelete(RedisConstants.SYSTEM_INITIALIZE_TOKEN, "expected")
    }

    @Test
    fun failedInitializationRetainsToken() = runTest {
        val redisService = redisServiceWithToken("expected")
        val transactionService = mock<SystemInitializeTransactionService> {
            onBlocking { isSystemInitialized() } doReturn false
            onBlocking { initializeSystem(any(), any(), any(), any(), any(), any(), any(), any(), any()) } doAnswer {
                throw IllegalStateException("failed")
            }
        }
        val service = SystemInitializeServiceImpl(redisService, transactionService)

        assertFailsWith<IllegalStateException> {
            initialize(service, "expected")
        }

        verify(redisService, never()).compareAndDelete(RedisConstants.SYSTEM_INITIALIZE_TOKEN, "expected")
    }

    @Test
    fun successfulInitializationConsumesTokenAfterTransactionReturns() = runTest {
        var transactionCompleted = false
        val redisService = redisServiceWithToken("expected")
        whenever(redisService.compareAndDelete(RedisConstants.SYSTEM_INITIALIZE_TOKEN, "expected"))
            .thenAnswer {
                check(transactionCompleted)
                Mono.just(true)
            }
        val transactionService = mock<SystemInitializeTransactionService> {
            onBlocking { isSystemInitialized() } doReturn false
            onBlocking { initializeSystem(any(), any(), any(), any(), any(), any(), any(), any(), any()) } doAnswer {
                transactionCompleted = true
            }
        }
        val service = SystemInitializeServiceImpl(redisService, transactionService)

        initialize(service, "expected")

        verify(redisService).compareAndDelete(RedisConstants.SYSTEM_INITIALIZE_TOKEN, "expected")
    }

    private suspend fun initialize(service: SystemInitializeService, token: String?) {
        service.initializeSystem(
            initializationToken = token,
            username = "root_user",
            password = "Password1",
            email = "root@example.com",
            smtpHost = "smtp.example.com",
            smtpPort = 587,
            smtpUsername = "root@example.com",
            smtpPassword = "smtp-password",
            fromEmail = "root@example.com",
            fromName = "Root",
        )
    }

    private fun redisServiceWithToken(token: String): ReactiveRedisService {
        return mock {
            on { setIfAbsent<String>(any(), any(), anyOrNull()) } doReturn Mono.just(true)
            on { get<String>(RedisConstants.SYSTEM_INITIALIZE_TOKEN) } doReturn Mono.just(token)
            on { compareAndDelete(any(), any()) } doReturn Mono.just(true)
        }
    }
}
