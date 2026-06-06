package com.lovelycatv.crystalframework.tenant.settings.constants

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType

object TenantSettingsConstants {
    object Notification {
        object MemberJoin {
            val EMAIL = SettingsItemDeclaration(
                key = "notification.memberJoin.email",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 1,
            )
        }

        object MemberJoinReview {
            val EMAIL = SettingsItemDeclaration(
                key = "notification.memberJoinReview.email",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 1,
            )
        }
    }
}
