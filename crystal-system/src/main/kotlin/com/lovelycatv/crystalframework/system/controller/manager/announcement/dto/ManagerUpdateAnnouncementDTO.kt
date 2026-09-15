/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

class ManagerUpdateAnnouncementDTO(
    override val id: Long = 0,
    @field:Size(max = 256)
    val title: String? = null,
    val content: String? = null,
    val status: Int? = null,
    val target: Int? = null,
    val priority: Int? = null,
) : BaseManagerUpdateDTO(id)
