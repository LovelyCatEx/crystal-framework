package com.lovelycatv.crystalframework.user.controller.manager.dto

import jakarta.validation.constraints.NotEmpty

data class ManagerForceLogoutUsersDTO(
    @field:NotEmpty(message = "userIds must not be empty")
    val userIds: List<Long>
)
