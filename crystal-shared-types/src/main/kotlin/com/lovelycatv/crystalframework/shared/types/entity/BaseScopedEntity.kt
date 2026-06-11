package com.lovelycatv.crystalframework.shared.types.entity

import org.springframework.data.relational.core.mapping.Column
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Base entity for resources that belong to a named scope.
 *
 * @property scope   The scope type identifier (corresponds to [ResourceScope.typeId])
 * @property scopeId The concrete ID within that scope (e.g. tenantId when scope = TENANT)
 */
abstract class BaseScopedEntity(
    id: Long = 0,
    @Column(value = "scope")
    var scope: Int = 0,
    @Column(value = "scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
