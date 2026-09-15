/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.repository.R2dbcRepository
import kotlin.reflect.KClass

interface BaseService<REPOSITORY: R2dbcRepository<ENTITY, Long>, ENTITY: BaseEntity> {
    fun getRepository(): REPOSITORY

    val entityClass: KClass<ENTITY>

    suspend fun getByIdOrNull(
        id: Long?
    ): ENTITY? {
        return id?.let { id ->
            this.getRepository()
                .findById(id)
                .awaitFirstOrNull()
        }
    }

    suspend fun getByIdOrThrow(
        id: Long,
        t: Throwable = BusinessException("Resource $id not found")
    ): ENTITY {
        return this.getByIdOrNull(id) ?: throw t
    }

    suspend fun withUpdateById(entityId: Long, action: suspend ENTITY.() -> Unit): ENTITY {
        val entity = this.getByIdOrThrow(entityId)

        action.invoke(entity)

        val after = this.getRepository()
            .save(entity)
            .awaitFirstOrNull()
            ?: throw BusinessException("Resource $entityId could not be save")

        return after
    }
}