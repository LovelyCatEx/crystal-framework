/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * Wallet balances are managed exclusively by the economy engine.
 * This DTO exists only to satisfy the generic type constraint of the readonly controller.
 */
data class ManagerUpdateWalletDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
