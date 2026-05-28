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
