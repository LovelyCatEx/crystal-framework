package com.lovelycatv.template.springboot.shared.repository

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.ParameterizedType
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

interface BaseRepository<ENTITY: BaseEntity> : R2dbcRepository<ENTITY, Long> {
    fun getTableName(): String {
        val entityClass = (this::class.supertypes
            .find {
                it.jvmErasure.supertypes.any {
                    it.jvmErasure == BaseRepository::class
                }
            }
            ?.jvmErasure
            ?.java
            ?.genericInterfaces
            ?.find {
                if (it is ParameterizedType) {
                    it.rawType == BaseRepository::class.java
                } else {
                    false
                }
            }
            as? ParameterizedType)
            ?.actualTypeArguments
            ?.firstOrNull()
            as? Class<*>

        val tableAnnotation = entityClass?.getAnnotation(Table::class.java)
        return tableAnnotation?.value
            ?: entityClass?.name?.lowercase(getDefault())
            ?: "UNKNOWN_ENTITY_TABLE_NAME"
    }

    @Query("SELECT * FROM #{#tableName} ORDER BY created_time DESC LIMIT :limit OFFSET :offset")
    fun findAllByPage(
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): Flux<ENTITY>

    @Query("""
        SELECT * FROM #{#tableName}
        WHERE ? LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """)
    fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<ENTITY>

    @Query("""
        SELECT COUNT(*) FROM #{#tableName}
        WHERE ? LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    fun countByKeyword(
        keyword: String
    ): Mono<Long>
}