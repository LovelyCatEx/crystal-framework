/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class ManagerCreateAnnouncementDTO(
    @field:NotBlank
    @field:Size(max = 256)
    var title: String = "",
    @field:NotBlank
    var content: String = "",
    var status: Int = 0,
    var target: Int = 2,
    var priority: Int = 0,
)
