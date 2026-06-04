package com.lovelycatv.crystalframework.tenant.settings.constants

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType

object TenantSettingsConstants {
    object Notification {
        val MEMBER_JOIN_NOTIFY_EMAIL = SettingsItemDeclaration(
            key = "notification.memberJoinNotifyEmail",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 1,
        )

        val MEMBER_JOIN_REVIEW_NOTIFY_EMAIL = SettingsItemDeclaration(
            key = "notification.memberJoinReviewNotifyEmail",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 2,
        )
    }
}