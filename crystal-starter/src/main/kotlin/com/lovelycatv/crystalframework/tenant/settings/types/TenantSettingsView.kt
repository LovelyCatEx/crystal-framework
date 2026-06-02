package com.lovelycatv.crystalframework.tenant.settings.types

data class TenantSettingsView(
    val notification: Notification,
) {
    data class Notification(
        val memberJoinNotifyEmail: Boolean,
        val memberJoinReviewNotifyEmail: Boolean,
    )
}
