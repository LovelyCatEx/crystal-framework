/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * Economy transactions are immutable and should not be updated.
 * This DTO exists only to satisfy the generic type constraint of the readonly controller.
 */
data class ManagerUpdateEconomyTransactionDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
