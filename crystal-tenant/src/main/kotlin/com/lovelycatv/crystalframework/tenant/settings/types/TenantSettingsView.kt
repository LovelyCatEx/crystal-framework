package com.lovelycatv.crystalframework.tenant.settings.types

data class TenantSettingsView(
    val notification: Notification,
) {
    data class Notification(
        val memberJoin: MemberJoin,
        val memberJoinReview: MemberJoinReview,
    ) {
        data class MemberJoin(
            val email: Boolean,
        )

        data class MemberJoinReview(
            val email: Boolean,
        )
    }
}
