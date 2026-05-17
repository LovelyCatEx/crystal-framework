package com.lovelycatv.crystalframework.auth.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * User login logs are immutable and should not be updated.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerUpdateUserLoginLogDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)