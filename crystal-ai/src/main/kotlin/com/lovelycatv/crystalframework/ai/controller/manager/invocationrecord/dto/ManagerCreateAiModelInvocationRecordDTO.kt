package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto

/**
 * AI model invocation records are created automatically by the system, not manually.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerCreateAiModelInvocationRecordDTO(
    val placeholder: String? = null
)
