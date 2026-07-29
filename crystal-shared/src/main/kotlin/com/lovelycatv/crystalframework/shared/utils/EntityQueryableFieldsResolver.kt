package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

/**
 * Resolves the set of column names that may appear in a client-supplied QueryNode
 * for a given entity class.
 *
 * A field is queryable when:
 *  1. It (or its Kotlin property) carries `@Column("...")`, AND
 *  2. It does NOT carry `@NotQueryable` on the field, property, or its Java field.
 *
 * Results are memoized per [KClass]. Also inherits from `BaseEntity` (which contributes
 * `id`, `created_time`, `modified_time`; `deleted_time` is intentionally NOT queryable
 * because it is managed by the soft-delete interceptor).
 */
object EntityQueryableFieldsResolver {
    private val cache: MutableMap<KClass<*>, Set<String>> = ConcurrentHashMap()
    private val BASE_ENTITY_QUERYABLE: Set<String> = setOf("id", "created_time", "modified_time")

    fun resolve(entityClass: KClass<out BaseEntity>): Set<String> =
        cache.getOrPut(entityClass) { computeQueryableFields(entityClass) }

    private fun computeQueryableFields(entityClass: KClass<out BaseEntity>): Set<String> {
        val fromEntity = entityClass.declaredMemberProperties
            .filter { !isNotQueryable(it) }
            .mapNotNull { extractColumnName(it) }
            .toSet()
        return fromEntity + BASE_ENTITY_QUERYABLE
    }

    private fun isNotQueryable(prop: KProperty1<*, *>): Boolean {
        if (prop.findAnnotation<NotQueryable>() != null) return true
        val javaField = prop.javaField ?: return false
        return javaField.isAnnotationPresent(NotQueryable::class.java)
    }

    private fun extractColumnName(prop: KProperty1<*, *>): String? {
        val onProperty = prop.findAnnotation<Column>()
        if (onProperty != null) return onProperty.value
        val javaField = prop.javaField ?: return null
        val onField = javaField.getAnnotation(Column::class.java)
        return onField?.value
    }
}
