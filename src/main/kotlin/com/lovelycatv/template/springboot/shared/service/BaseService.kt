package com.lovelycatv.template.springboot.shared.service

import com.lovelycatv.template.springboot.shared.exception.BusinessException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface BaseService<REPOSITORY: ReactiveCrudRepository<ENTITY, ID>, ENTITY: Any, ID: Any> {
    fun getRepository(): REPOSITORY

    suspend fun getByIdOrThrow(
        id: ID,
        t: Throwable = BusinessException("Resource $id not found")
    ): ENTITY {
        return this.getRepository()
            .findById(id)
            .awaitFirstOrNull()
            ?: throw t
    }
}