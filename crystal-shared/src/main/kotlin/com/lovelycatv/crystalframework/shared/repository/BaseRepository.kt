package com.lovelycatv.crystalframework.shared.repository

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.mapping.Table
import java.lang.reflect.ParameterizedType
import java.util.Locale.getDefault
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
}
