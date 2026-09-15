/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.manager.dto

/**
 * User login logs are created automatically by the system, not manually.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerCreateUserLoginLogDTO(
    val placeholder: String? = null
)