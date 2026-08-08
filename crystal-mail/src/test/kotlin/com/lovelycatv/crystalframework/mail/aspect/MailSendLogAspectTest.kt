package com.lovelycatv.crystalframework.mail.aspect

import com.lovelycatv.crystalframework.mail.service.MailSendLogService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.context.CurrentTenantId
import com.lovelycatv.crystalframework.shared.context.CurrentUserId
import kotlinx.coroutines.reactor.ReactorContext
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import reactor.util.context.Context
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class MailSendLogAspectTest {
    @Test
    fun `captures user and tenant from suspend continuation context`() {
        val recorded = CountDownLatch(1)
        var recordedUserId: Long? = null
        var recordedTenantId: Long? = null
        val service = MailSendLogService { _, _, _, _, _, _, userId, tenantId ->
            recordedUserId = userId
            recordedTenantId = tenantId
            recorded.countDown()
        }
        val aspect = MailSendLogAspect(service, mockSystemModuleClient())
        val userId = 101L
        val tenantId = 202L
        val joinPoint = joinPoint(
            args = arrayOf("to@example.com", "subject", "content", continuation(userId, tenantId)),
            result = "sent",
        )

        assertEquals("sent", aspect.recordMailSendLog(joinPoint))
        assertEquals(true, recorded.await(2, TimeUnit.SECONDS))
        assertEquals(userId, recordedUserId)
        assertEquals(tenantId, recordedTenantId)
    }

    @Test
    fun `records null identities without continuation context`() {
        val recorded = CountDownLatch(1)
        var recordedUserId: Long? = 1L
        var recordedTenantId: Long? = 2L
        val service = MailSendLogService { _, _, _, _, _, _, userId, tenantId ->
            recordedUserId = userId
            recordedTenantId = tenantId
            recorded.countDown()
        }
        val aspect = MailSendLogAspect(service, mockSystemModuleClient())
        val joinPoint = joinPoint(
            args = arrayOf("to@example.com", "subject", "content"),
            result = "sent",
        )

        assertEquals("sent", aspect.recordMailSendLog(joinPoint))
        assertEquals(true, recorded.await(2, TimeUnit.SECONDS))
        assertEquals(null, recordedUserId)
        assertEquals(null, recordedTenantId)
    }

    @Test
    fun `preserves mail failure while recording failed result`() {
        val recorded = CountDownLatch(1)
        var recordedSuccess: Boolean? = null
        var recordedError: String? = null
        val service = MailSendLogService { _, _, _, _, success, errorMessage, _, _ ->
            recordedSuccess = success
            recordedError = errorMessage
            recorded.countDown()
        }
        val aspect = MailSendLogAspect(service, mockSystemModuleClient())
        val failure = IllegalStateException("send failed")
        val joinPoint = joinPoint(
            args = arrayOf("to@example.com", "subject", "content"),
            error = failure,
        )

        val thrown = runCatching { aspect.recordMailSendLog(joinPoint) }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(true, recorded.await(2, TimeUnit.SECONDS))
        assertEquals(false, recordedSuccess)
        assertEquals("send failed", recordedError)
    }

    private fun mockSystemModuleClient(): SystemModuleClient = mock {
        on { getSystemSettings() } doReturn null
    }

    private fun continuation(userId: Long, tenantId: Long): Continuation<Any?> {
        val context = Context.empty()
            .put(CurrentUserId, userId)
            .put(CurrentTenantId, tenantId)
        return object : Continuation<Any?> {
            override val context: CoroutineContext = ReactorContext(context)

            override fun resumeWith(result: Result<Any?>) = Unit
        }
    }

    private fun joinPoint(
        args: Array<Any>,
        result: Any? = null,
        error: Throwable? = null,
    ): ProceedingJoinPoint = mock {
        on { this.args } doReturn args
        if (error == null) {
            on { proceed() } doReturn result
        } else {
            on { proceed() } doAnswer { throw error }
        }
    }
}
