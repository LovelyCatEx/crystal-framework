/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.entity

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A per-owner, per-currency balance account. `scope`/`scopeId` locate the resource the owner lives
 * in ([ResourceScope.SYSTEM] with `scopeId` = 0, or [ResourceScope.TENANT] with `scopeId` =
 * tenantId), while `ownerId` identifies the concrete owner within that scope (a user id for
 * SYSTEM, a tenant member id for TENANT). Wallets are lazily created by the engine on first use.
 */
@Table("economy_wallets")
class WalletEntity(
    id: Long = 0,
    scope: Int = ResourceScope.SYSTEM.typeId,
    scopeId: Long = 0,
    @Column("owner_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var ownerId: Long = 0,
    @Column("currency_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var currencyId: Long = 0,
    @Column("balance")
    var balance: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime)
