package com.lovelycatv.crystalframework.tenant.settings.config

import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.sdk.tenant.settings.config.TenantSettingsConfigurer
import com.lovelycatv.crystalframework.tenant.settings.constants.TenantSettingsConstants
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TenantSettingsBuiltinConfigurer : TenantSettingsConfigurer {
    override fun configure(registry: TenantSettingsRegistry) {
        registry.settings(
            listOf(
                TenantSettingsConstants.Notification.MEMBER_JOIN_NOTIFY_EMAIL,
                TenantSettingsConstants.Notification.MEMBER_JOIN_REVIEW_NOTIFY_EMAIL,
            )
        )
    }
}
