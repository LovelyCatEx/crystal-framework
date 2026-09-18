/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.config.observability

import com.lovelycatv.crystalframework.shared.context.CurrentTenantId
import com.lovelycatv.crystalframework.shared.context.CurrentUserId
import org.reactivestreams.Subscription
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import reactor.core.CoreSubscriber
import reactor.core.Scannable
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators
import reactor.util.context.Context

/**
 * Bridges the per-request [CurrentUserId] / [CurrentTenantId] Reactor Context entries into the SLF4J
 * MDC on every operator boundary, so structured-log fields `auth.user.id` / `auth.user.tenant.id`
 * are present on every log statement in the request — not just the SQL audit line — and the Elastic
 * ECS encoder serializes them into queryable fields.
 *
 * MDC is thread-local and does not survive Reactor thread hops, so this registers a global
 * [Hooks.onEachOperator] decorator that restores the two keys from the operator's downstream context
 * around each signal, then restores the previous values.
 */
@Configuration(proxyBeanMethods = false)
class MdcContextPropagationConfig {

    init {
        Hooks.onEachOperator(HOOK_KEY, Operators.lift<Any, Any> { _: Scannable, actual: CoreSubscriber<in Any> ->
            MdcSubscriber(actual)
        })
    }

    private class MdcSubscriber(
        private val actual: CoreSubscriber<in Any>,
    ) : CoreSubscriber<Any> {

        private var context: Context = Context.empty()

        override fun currentContext(): Context = actual.currentContext()

        override fun onSubscribe(s: Subscription) {
            context = actual.currentContext()
            withMdc { actual.onSubscribe(s) }
        }

        override fun onNext(t: Any) = withMdc { actual.onNext(t) }

        override fun onError(t: Throwable) = withMdc { actual.onError(t) }

        override fun onComplete() = withMdc { actual.onComplete() }

        private fun <R> withMdc(block: () -> R): R {
            val userId = CurrentUserId.from(context)?.toString()
            val tenantId = CurrentTenantId.from(context)?.toString()
            val previousUserId = MDC.get(MDC_AUTH_USER_ID)
            val previousTenantId = MDC.get(MDC_AUTH_USER_TENANT_ID)
            return try {
                putOrRemove(MDC_AUTH_USER_ID, userId)
                putOrRemove(MDC_AUTH_USER_TENANT_ID, tenantId)
                block()
            } finally {
                putOrRemove(MDC_AUTH_USER_ID, previousUserId)
                putOrRemove(MDC_AUTH_USER_TENANT_ID, previousTenantId)
            }
        }
    }

    companion object {
        private const val HOOK_KEY = "crystal-mdc"
        private const val MDC_AUTH_USER_ID = "auth.user.id"
        private const val MDC_AUTH_USER_TENANT_ID = "auth.user.tenant.id"

        private fun putOrRemove(key: String, value: String?) {
            if (value == null) MDC.remove(key) else MDC.put(key, value)
        }
    }
}
