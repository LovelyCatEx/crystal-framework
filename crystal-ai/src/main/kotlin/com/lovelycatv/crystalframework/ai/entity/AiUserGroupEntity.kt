/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("ai_user_groups")
class AiUserGroupEntity(
    id: Long = 0,
    @Column("name")
    var name: String = "",
    @Column("key")
    var key: String = "",
    @Column("description")
    var description: String? = null,
    @Column("billing_multiplier")
    var billingMultiplier: BigDecimal = BigDecimal.ONE,
    @Column("enabled")
    var enabled: Boolean = true,
    @Column("is_default")
    var isDefault: Boolean = false,
    @Column("sort")
    var sort: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
