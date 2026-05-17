package com.lovelycatv.crystalframework.system.filter

import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.system.controller.ReadinessController
import kotlinx.coroutines.reactor.mono
import org.springframework.core.annotation.Order
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

            mono {
                RbacUtils.hasAuthority(MAINTENANCE_ACCESS_PERMISSION)
            }.flatMap { hasAccessPermission ->
                if (!hasAccessPermission) {
                    throw BusinessException("System is under maintenance and is temporarily unavailable.")
                } else {
                    chain.filter(exchange)
                }
            }
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