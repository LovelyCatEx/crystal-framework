package com.lovelycatv.crystalframework.auth.controller.manager.dto

/**
 * User login logs are created automatically by the system, not manually.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerCreateUserLoginLogDTO(
    val placeholder: String? = null
)