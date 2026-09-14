/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * AI model invocation records are immutable and should not be updated.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerUpdateAiModelInvocationRecordDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
