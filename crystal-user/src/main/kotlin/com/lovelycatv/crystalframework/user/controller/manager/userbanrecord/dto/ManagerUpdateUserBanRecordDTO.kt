/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * Ban records are immutable through the manager API; lifting a ban goes through the unban business flow.
 * This DTO exists only to satisfy the generic type constraint of the manager controller family.
 */
data class ManagerUpdateUserBanRecordDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
