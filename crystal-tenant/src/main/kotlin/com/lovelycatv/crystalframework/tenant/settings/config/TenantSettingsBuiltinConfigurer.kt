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
                TenantSettingsConstants.Notification.MemberJoin.EMAIL,
                TenantSettingsConstants.Notification.MemberJoin.CHANNELS,
                TenantSettingsConstants.Notification.MemberJoin.CONTENT,
                TenantSettingsConstants.Notification.MemberJoinReview.EMAIL,
                TenantSettingsConstants.Notification.MemberJoinReview.CHANNELS,
                TenantSettingsConstants.Notification.MemberJoinReview.CONTENT,
            )
        )
    }
}
