package com.lovelycatv.crystalframework.system.filter

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Outcome
import com.lovelycatv.crystalframework.shared.config.observability.ApmSpanConstants
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
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

            // Only reached while the system is in maintenance and the path is not allow-listed; this is
            // the sole branch that runs an RBAC authority check. Normal (non-maintenance) traffic returns
            // via the else branch below without a span, so it never emits noise.
            val span = ElasticApm.currentSpan()
                .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, ApmSpanConstants.SPAN_ACTION)
                .setName(MAINTENANCE_GUARD_SPAN_NAME)

            mono {
                RbacUtils.hasAuthority(MAINTENANCE_ACCESS_PERMISSION)
            }
                .doOnError { error ->
                    span.captureException(error)
                    span.setOutcome(Outcome.FAILURE)
                }
                .doFinally { span.end() }
                .flatMap { hasAccessPermission ->
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
        // Brackets the maintenance-access RBAC check, started only inside the maintenance branch so
        // ordinary traffic (system not in maintenance) never emits a span.
        private const val MAINTENANCE_GUARD_SPAN_NAME = "SystemMaintenanceGuardFilter#checkMaintenanceAccess"

        val MAINTENANCE_ACCESS_PERMISSION: String = SystemPermission.ACTION_SYSTEM_MAINTENANCE_ACCESS.name
        private val pathPatternParser = PathPatternParser()
        private val allowList = listOf(
            pathPatternParser.parse("/api/*/manager/system/maintenance")
        )
    }
}