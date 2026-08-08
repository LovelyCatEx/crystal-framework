package com.lovelycatv.crystalframework.system.filter

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Outcome
import com.lovelycatv.crystalframework.shared.config.observability.ApmSpanConstants
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemModulePathConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import kotlinx.coroutines.reactor.mono
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

@Order(GlobalConstants.FilterPriority.SYSTEM_MODULE_GUARD)
@Component
class SystemModuleGuardFilter(
    private val systemSettingsService: SystemSettingsService,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val requestPath = exchange.request.path.pathWithinApplication()

        val matchesTenant = TENANT_PATTERNS.any { it.matches(requestPath) }
        val matchesApproval = APPROVAL_PATTERNS.any { it.matches(requestPath) }

        if (!matchesTenant && !matchesApproval) {
            return chain.filter(exchange)
        }

        // Only the matched-path branch does real work (a cached system-module settings lookup); the
        // fast-path return above stays unspanned so the common request never emits a noise span.
        val span = ElasticApm.currentSpan()
            .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, ApmSpanConstants.SPAN_ACTION)
            .setName(MODULE_GUARD_SPAN_NAME)

        return mono { systemSettingsService.getSystemModuleSettings() }
            .doOnError { error ->
                span.captureException(error)
                span.setOutcome(Outcome.FAILURE)
            }
            .doFinally { span.end() }
            .flatMap { module ->
                when {
                    matchesApproval && !module.approvalEnabled ->
                        throw BusinessException("Approval module is disabled by administrator")
                    matchesTenant && !module.tenantEnabled ->
                        throw BusinessException("Tenant module is disabled by administrator")
                    else -> chain.filter(exchange)
                }
            }
    }

    companion object {
        // Brackets the cached system-module settings lookup performed only when the request path is a
        // tenant / approval endpoint. Started inside the matched branch so fast-path traffic stays clean.
        private const val MODULE_GUARD_SPAN_NAME = "SystemModuleGuardFilter#checkModuleEnabled"

        private val parser = PathPatternParser()

        private val TENANT_PATTERNS =
            SystemModulePathConstants.Tenant.apiPathPatterns.map { parser.parse(it) }

        private val APPROVAL_PATTERNS =
            SystemModulePathConstants.Approval.apiPathPatterns.map { parser.parse(it) }
    }
}
