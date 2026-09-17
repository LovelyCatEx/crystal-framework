/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.entity

import com.lovelycatv.crystalframework.economy.types.EconomyReferenceType
import com.lovelycatv.crystalframework.economy.types.EconomyTransactionType
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Append-only ledger entry recording one balance change. `scope`/`scopeId`/`ownerId` mirror the
 * wallet the change applied to. `type` and `referenceType` store registry typeIds (see
 * `EconomyTransactionTypeRegistry` / `EconomyReferenceTypeRegistry`); `amount` is signed
 * (positive = credit, negative = debit).
 */
@Table("economy_transactions")
class EconomyTransactionEntity(
    id: Long = 0,
    scope: Int = ResourceScope.SYSTEM.typeId,
    scopeId: Long = 0,
    @Column("owner_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var ownerId: Long = 0,
    @Column("request_id")
    var requestId: String = "",
    @Column("type")
    var type: Int = EconomyTransactionType.RECHARGE.typeId,
    @Column("currency_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var currencyId: Long = 0,
    @Column("amount")
    var amount: Long = 0,
    @Column("balance_before")
    var balanceBefore: Long = 0,
    @Column("balance_after")
    var balanceAfter: Long = 0,
    @Column("reference_type")
    var referenceType: Int = EconomyReferenceType.NONE.typeId,
    @Column("reference_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var referenceId: Long? = null,
    @Column("remark")
    var remark: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime)
