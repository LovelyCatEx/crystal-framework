/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto

/**
 * Ban records are created by the ban business flow, not through the standard manager create endpoint.
 * This DTO exists only to satisfy the generic type constraint of the manager controller family.
 */
data class ManagerCreateUserBanRecordDTO(
    val placeholder: String? = null
)
