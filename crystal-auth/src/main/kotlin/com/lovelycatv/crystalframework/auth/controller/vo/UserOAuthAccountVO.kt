/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.vo

data class UserOAuthAccountVO(
    val id: Long,
    val platformId: Int,
    val nickname: String?,
    val avatar: String?,
)