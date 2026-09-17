/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.entity

import com.lovelycatv.crystalframework.economy.types.CurrencySymbolPosition
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("economy_currencies")
class CurrencyEntity(
    id: Long = 0,
    @Column("code")
    var code: String = "",
    @Column("name")
    var name: String = "",
    @Column("symbol")
    var symbol: String = "",
    @Column("precision")
    var precision: Int = 2,
    @Column("symbol_position")
    var symbolPosition: Int = CurrencySymbolPosition.PREFIX.typeId,
    @Column("decimal_separator")
    var decimalSeparator: String = ".",
    @Column("thousands_separator")
    var thousandsSeparator: String = ",",
    @Column("description")
    var description: String? = null,
    @Column("enabled")
    var enabled: Boolean = true,
    @Column("sort")
    var sort: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
