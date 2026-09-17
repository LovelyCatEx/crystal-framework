/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.types

import com.lovelycatv.crystalframework.sdk.economy.types.EconomyTransactionTypeDeclaration

/**
 * Built-in economy transaction types, published as the first batch of
 * [EconomyTransactionTypeDeclaration]s registered in `EconomyTransactionTypeRegistry`. The enum
 * stays for compile-time convenience inside the framework (`EconomyTransactionType.RECHARGE.typeId`),
 * while runtime dispatch always goes through the registry so third-party types participate on
 * equal footing.
 *
 * Only the two fundamental directions of value movement are reserved here (0 = credit, 1 = debit);
 * anything richer (grant, reverse, transfer, exchange, ...) is contributed by developers with a
 * `typeId >= 1000`. `typeId` values are frozen — they land in `economy_transactions.type`.
 */
enum class EconomyTransactionType(
    override val typeId: Int,
    override val key: String,
    override val displayName: String,
    override val description: String,
) : EconomyTransactionTypeDeclaration {
    RECHARGE(
        typeId = 0,
        key = "builtin_recharge",
        displayName = "Recharge",
        description = "Credit funds into a wallet.",
    ),
    DEDUCT(
        typeId = 1,
        key = "builtin_deduct",
        displayName = "Deduct",
        description = "Debit funds out of a wallet.",
    ),
    ;

    companion object {
        fun getByTypeId(typeId: Int): EconomyTransactionType? = entries.find { it.typeId == typeId }
    }
}
