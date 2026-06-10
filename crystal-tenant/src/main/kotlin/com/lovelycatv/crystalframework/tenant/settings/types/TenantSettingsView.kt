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
            val channels: List<Long>,
            val content: String,
        )

        data class MemberJoinReview(
            val email: Boolean,
            val channels: List<Long>,
            val content: String,
        )
    }
}
