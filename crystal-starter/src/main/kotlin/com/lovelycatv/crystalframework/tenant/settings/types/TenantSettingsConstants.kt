package com.lovelycatv.crystalframework.tenant.settings.types

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType

object TenantSettingsConstants {
    object Notification {
        val MEMBER_JOIN_NOTIFY_EMAIL = SystemSettingsItemDeclaration(
            key = "notification.memberJoinNotifyEmail",
            valueType = SystemSettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 1,
        )
    }
}
