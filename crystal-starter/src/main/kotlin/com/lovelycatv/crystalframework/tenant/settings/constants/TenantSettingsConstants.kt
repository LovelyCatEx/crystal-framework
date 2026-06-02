package com.lovelycatv.crystalframework.tenant.settings.constants

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

        val MEMBER_JOIN_REVIEW_NOTIFY_EMAIL = SystemSettingsItemDeclaration(
            key = "notification.memberJoinReviewNotifyEmail",
            valueType = SystemSettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 2,
        )
    }
}