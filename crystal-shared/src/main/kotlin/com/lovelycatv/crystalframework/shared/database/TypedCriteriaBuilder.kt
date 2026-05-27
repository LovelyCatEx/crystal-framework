package com.lovelycatv.crystalframework.shared.database

import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class TypedCriteriaBuilder<T : Any>(private val entityClass: KClass<T>) {
    private val columnMap = EntityColumnCache.getColumnMap(entityClass)
    private var criteria: Criteria = Criteria.empty()

    fun <V : Any> eq(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).`is`(value))
        return this
    }

    fun <V : Any> ne(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).not(value))
        return this
    }

    fun like(property: KProperty1<T, String>, pattern: String): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).like(pattern))
        return this
    }

    fun contains(property: KProperty1<T, String>, value: String): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).like("%$value%"))
        return this
    }

    fun <V : Comparable<V>> gt(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).greaterThan(value))
        return this
    }

    fun <V : Comparable<V>> gte(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).greaterThanOrEquals(value))
        return this
    }

    fun <V : Comparable<V>> lt(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).lessThan(value))
        return this
    }

    fun <V : Comparable<V>> lte(property: KProperty1<T, V>, value: V): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).lessThanOrEquals(value))
        return this
    }

    fun <V> `in`(property: KProperty1<T, V>, values: Collection<V>): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).`in`(values))
        return this
    }

    fun isNull(property: KProperty1<T, *>): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).isNull())
        return this
    }

    fun isNotNull(property: KProperty1<T, *>): TypedCriteriaBuilder<T> {
        val columnName = columnMap[property] ?: property.name
        criteria = criteria.and(Criteria.where(columnName).isNotNull())
        return this
    }

    fun or(builder: TypedCriteriaBuilder<T>.() -> Unit): TypedCriteriaBuilder<T> {
        val subBuilder = TypedCriteriaBuilder(entityClass)
        subBuilder.builder()
        criteria = criteria.or(subBuilder.build())
        return this
    }

    fun and(builder: TypedCriteriaBuilder<T>.() -> Unit): TypedCriteriaBuilder<T> {
        builder()
        return this
    }

    fun build(): Criteria {
        return criteria
    }
}
