/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto

/**
 * Wallets are created automatically by the economy engine, not manually.
 * This DTO exists only to satisfy the generic type constraint of the readonly controller.
 */
data class ManagerCreateWalletDTO(
    val placeholder: String? = null
)
