package com.lovelycatv.crystalframework.audit.aspect

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.service.AuditLogRecorder
import com.lovelycatv.crystalframework.audit.service.AuditLogService
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AnnotatedControllerAuditAspectTest {
    private val authentication = UserAuthentication(
        userId = 1L,
        username = "tester",
        tenantId = 2L,
        tenantMemberId = 3L,
    )

    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
        resourceIds = "#request.resourceId",
    )
    private fun annotatedEndpoint(
        userAuthentication: UserAuthentication,
        request: TestRequest,
    ): Mono<String> = Mono.just("ok")

    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
        resourceIds = "#request.invalidResourceIds",
    )
    private fun invalidCollectionEndpoint(
        userAuthentication: UserAuthentication,
        request: TestRequest,
    ): Mono<String> = Mono.just("ok")

    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
    )
    private fun synchronousEndpoint(
        userAuthentication: UserAuthentication,
        request: TestRequest,
    ): String = "ok"

    @Test
    fun `records annotated method success once`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val aspect = AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch)))
        val request = TestRequest(42L)
        val joinPoint = joinPointFor(
            methodName = "annotatedEndpoint",
            args = arrayOf(authentication, request),
            result = annotatedEndpoint(authentication, request),
        )

        @Suppress("UNCHECKED_CAST")
        val result = aspect.audit(joinPoint) as Mono<String>
        assertEquals("ok", result.contextWrite(requestContext()).block())
        latch.await(2, TimeUnit.SECONDS)

        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, listOf(42L), 10L, true, null)),
            records,
        )
    }

    @Test
    fun `records synchronous method success once`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val aspect = AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch)))
        val request = TestRequest(42L)
        val joinPoint = joinPointFor(
            methodName = "synchronousEndpoint",
            args = arrayOf(authentication, request),
            result = "ok",
        )

        assertEquals("ok", aspect.audit(joinPoint))
        latch.await(2, TimeUnit.SECONDS)

        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, null, null, true, null)),
            records,
        )
    }

    @Test
    fun `records synchronous method failure once and preserves error`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val aspect = AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch)))
        val failure = IllegalStateException("boom")
        val joinPoint = joinPointFor(
            methodName = "synchronousEndpoint",
            args = arrayOf(authentication, TestRequest(42L)),
            error = failure,
        )

        val thrown = runCatching { aspect.audit(joinPoint) }.exceptionOrNull()
        latch.await(2, TimeUnit.SECONDS)

        assertSame(failure, thrown)
        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, null, null, false, "boom")),
            records,
        )
    }

    @Test
    fun `spring aop proxy applies audit annotation`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val target = TestController()
        val factory = AspectJProxyFactory(target)
        factory.addAspect(AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch))))
        val proxy = factory.getProxy<TestController>()

        assertEquals("ok", proxy.synchronous(authentication, TestRequest(42L)))
        latch.await(2, TimeUnit.SECONDS)

        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, listOf(42L), null, true, null)),
            records,
        )
    }

    @Test
    fun `spring aop proxy applies audit annotation to suspend method`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val target = TestController()
        val factory = AspectJProxyFactory(target)
        factory.addAspect(AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch))))
        val proxy = factory.getProxy<TestController>()

        assertEquals("ok", runBlocking { proxy.suspending(authentication, TestRequest(42L)) })
        latch.await(2, TimeUnit.SECONDS)

        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, listOf(42L), null, true, null)),
            records,
        )
    }

    @Test
    fun `spring aop proxy records suspend failure after suspension`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val target = TestController()
        val factory = AspectJProxyFactory(target)
        factory.addAspect(AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch))))
        val proxy = factory.getProxy<TestController>()

        val thrown = runCatching {
            runBlocking { proxy.suspendingFailure(authentication, TestRequest(42L)) }
        }.exceptionOrNull()
        latch.await(2, TimeUnit.SECONDS)

        assertEquals("boom", thrown?.message)
        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, listOf(42L), null, false, "boom")),
            records,
        )
    }

    @Test
    fun `rejects resource id collections containing non Long values`() {
        val aspect = AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(mutableListOf(), CountDownLatch(1))))
        val request = TestRequest(42L)
        val joinPoint = joinPointFor(
            methodName = "invalidCollectionEndpoint",
            args = arrayOf(authentication, request),
            result = annotatedEndpoint(authentication, request),
        )

        val thrown = runCatching { aspect.audit(joinPoint) }.exceptionOrNull()

        assertEquals("Audit resourceIds expression must resolve to Long or Collection<Long>", thrown?.message)
    }

    @Test
    fun `records annotated method failure once and preserves error`() {
        val records = mutableListOf<RecordedAudit>()
        val latch = CountDownLatch(1)
        val aspect = AnnotatedControllerAuditAspect(AuditLogRecorder(recordingService(records, latch)))
        val failure = IllegalStateException("boom")
        val joinPoint = joinPointFor(
            methodName = "annotatedEndpoint",
            args = arrayOf(authentication, TestRequest(42L)),
            result = Mono.error<String>(failure),
        )

        @Suppress("UNCHECKED_CAST")
        val result = aspect.audit(joinPoint) as Mono<String>
        val thrown = runCatching { result.contextWrite(requestContext()).block() }.exceptionOrNull()
        latch.await(2, TimeUnit.SECONDS)

        assertSame(failure, thrown)
        assertEquals(
            listOf(RecordedAudit(AuditAction.READ, TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, listOf(42L), 10L, false, "boom")),
            records,
        )
    }

    private fun recordingService(
        records: MutableList<RecordedAudit>,
        latch: CountDownLatch,
    ) = AuditLogService { _, requestInfo, action, resourceType, resourceIds, success, errorMessage ->
        records += RecordedAudit(action, resourceType, resourceIds, requestInfo?.requestId, success, errorMessage)
        latch.countDown()
    }

    private fun joinPointFor(
        methodName: String,
        args: Array<Any>,
        result: Any? = null,
        error: Throwable? = null,
    ): ProceedingJoinPoint {
        val method = javaClass.getDeclaredMethod(
            methodName,
            UserAuthentication::class.java,
            TestRequest::class.java,
        )
        val signature = mock<MethodSignature> {
            on { this.method } doReturn method
            on { parameterNames } doReturn arrayOf("userAuthentication", "request")
        }
        return mock {
            on { this.args } doReturn args
            on { this.signature } doReturn signature
            if (error == null) {
                on { proceed() } doReturn result
            } else {
                on { proceed() } doAnswer { throw error }
            }
        }
    }

    private fun requestContext(): (Context) -> Context = { context ->
        context.put(
            AuditRequestContext.KEY,
            AuditRequestInfo(10L, "POST", "/test", "127.0.0.1", "test"),
        )
    }

    open class TestController {
        @Audit(
            action = AuditAction.READ,
            resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
            resourceIds = "#request.resourceId",
        )
        open fun synchronous(
            userAuthentication: UserAuthentication,
            request: TestRequest,
        ): String = "ok"

        @Audit(
            action = AuditAction.READ,
            resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
            resourceIds = "#request.resourceId",
        )
        open suspend fun suspending(
            userAuthentication: UserAuthentication,
            request: TestRequest,
        ): String = "ok"

        @Audit(
            action = AuditAction.READ,
            resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
            resourceIds = "#request.resourceId",
        )
        open suspend fun suspendingFailure(
            userAuthentication: UserAuthentication,
            request: TestRequest,
        ): String {
            yield()
            error("boom")
        }
    }

    data class TestRequest(
        val resourceId: Long,
        val invalidResourceIds: List<Any> = listOf(resourceId, "invalid"),
    )

    private data class RecordedAudit(
        val action: AuditAction,
        val resourceType: String,
        val resourceIds: List<Long>?,
        val requestId: Long?,
        val success: Boolean,
        val errorMessage: String?,
    )
}
