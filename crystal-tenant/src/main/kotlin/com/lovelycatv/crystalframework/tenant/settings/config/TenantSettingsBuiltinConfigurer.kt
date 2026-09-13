/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
