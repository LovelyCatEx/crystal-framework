/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.vo

data class UserProfileVO(
    val id: Long,
    val nickname: String,
    val avatar: String?,
    val username: String?,
    val email: String?,
    val registeredTime: Long?
)
