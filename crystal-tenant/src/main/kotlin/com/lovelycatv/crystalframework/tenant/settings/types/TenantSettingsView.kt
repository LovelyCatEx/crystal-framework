/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
