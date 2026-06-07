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

            val CHANNELS = SettingsItemDeclaration(
                key = "notification.memberJoin.channels",
                valueType = SettingsItemValueType.NUMBER_ARRAY,
                defaultValue = "[]",
                sort = 2,
            )

            val CONTENT = SettingsItemDeclaration(
                key = "notification.memberJoin.content",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 3,
            )
        }

        object MemberJoinReview {
            val EMAIL = SettingsItemDeclaration(
                key = "notification.memberJoinReview.email",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 1,
            )

            val CHANNELS = SettingsItemDeclaration(
                key = "notification.memberJoinReview.channels",
                valueType = SettingsItemValueType.NUMBER_ARRAY,
                defaultValue = "[]",
                sort = 2,
            )

            val CONTENT = SettingsItemDeclaration(
                key = "notification.memberJoinReview.content",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 3,
            )
        }
    }
}
