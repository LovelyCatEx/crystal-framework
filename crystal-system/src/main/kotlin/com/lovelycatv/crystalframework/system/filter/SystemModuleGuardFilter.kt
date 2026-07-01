package com.lovelycatv.crystalframework.system.filter

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

        return mono { systemSettingsService.getSystemModuleSettings() }.flatMap { module ->
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
        private val parser = PathPatternParser()

        private val TENANT_PATTERNS =
            SystemModulePathConstants.Tenant.apiPathPatterns.map { parser.parse(it) }

        private val APPROVAL_PATTERNS =
            SystemModulePathConstants.Approval.apiPathPatterns.map { parser.parse(it) }
    }
}
