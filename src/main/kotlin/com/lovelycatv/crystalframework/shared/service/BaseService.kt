package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface BaseService<REPOSITORY: R2dbcRepository<ENTITY, Long>, ENTITY: BaseEntity> {
    fun getRepository(): REPOSITORY

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