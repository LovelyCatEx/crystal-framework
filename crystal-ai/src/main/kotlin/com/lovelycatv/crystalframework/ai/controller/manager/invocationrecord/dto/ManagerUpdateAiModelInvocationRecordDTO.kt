package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * AI model invocation records are immutable and should not be updated.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerUpdateAiModelInvocationRecordDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
