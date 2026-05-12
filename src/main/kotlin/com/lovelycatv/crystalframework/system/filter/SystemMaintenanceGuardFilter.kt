package com.lovelycatv.crystalframework.system.filter

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.controller.ReadinessController
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

@Order(GlobalConstants.FilterPriority.SYSTEM_MAINTENANCE_GUARD)
@Component
class SystemMaintenanceGuardFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        return if (ReadinessController.isInMaintenance()) {
            val requestPath = exchange.request.path.pathWithinApplication()

            if (allowList.any { it.matches(requestPath) }) {
                return chain.filter(exchange)
            }

            ReactiveSecurityContextHolder.getContext()
                .flatMap { securityContext ->
                    val authorities = securityContext.authentication?.authorities
                        ?.mapNotNull { it.authority } ?: emptyList()
                    val hasAccessPermission = authorities.contains(MAINTENANCE_ACCESS_PERMISSION)

                    if (!hasAccessPermission) {
                        Mono.error(BusinessException("System is under maintenance and is temporarily unavailable."))
                    } else {
                        chain.filter(exchange)
                    }
                }
                // No SecurityContext (unauthenticated / @Unauthorized path) → block with maintenance message
                .switchIfEmpty(Mono.defer {
                    Mono.error(BusinessException("System is under maintenance and is temporarily unavailable."))
                })
        } else {
            chain.filter(exchange)
        }
    }

    companion object {
        const val MAINTENANCE_ACCESS_PERMISSION = SystemPermission.ACTION_SYSTEM_MAINTENANCE_ACCESS
        private val pathPatternParser = PathPatternParser()
        private val allowList = listOf(
            pathPatternParser.parse("/api/*/manager/system/maintenance"),
            pathPatternParser.parse("/api/*/actuator/**")
        )
    }
}
