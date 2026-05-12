package com.lovelycatv.crystalframework.system.filter

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.system.controller.ReadinessController
import kotlinx.coroutines.reactor.mono
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Order(Ordered.LOWEST_PRECEDENCE)
@Component
class SystemMaintenanceGuardFilter : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        return if (ReadinessController.isInMaintenance()) {
            mono {
                RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_MAINTENANCE_ACCESS)
            }.map { hasAccessPermission ->
                if (!hasAccessPermission) {
                    throw BusinessException("System is under maintenance and is temporarily unavailable.")
                } else {
                    chain.filter(exchange)
                }
            }.then()
        } else {
            chain.filter(exchange)
        }
    }
}