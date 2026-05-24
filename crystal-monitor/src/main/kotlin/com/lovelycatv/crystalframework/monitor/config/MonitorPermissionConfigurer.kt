package com.lovelycatv.crystalframework.monitor.config

import com.lovelycatv.crystalframework.monitor.constants.MonitorPermission
import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import org.springframework.stereotype.Component

@Component
class MonitorPermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.permission(
            SystemRbacPermissionDeclaration.menu(
                name = MonitorPermission.MENU_SYSTEM_MONITOR,
                path = "/manager/monitor/system-metrics",
            ),
        )
        registry.permission(
            SystemRbacPermissionDeclaration.action(
                name = MonitorPermission.ACTION_SYSTEM_MONITOR_READ,
            ),
        )
        registry.grantAll(SystemRole.ROLE_ADMIN)
    }
}
