package com.lovelycatv.crystalframework.shared.database

import org.springframework.data.relational.core.query.Criteria

class CrystalDatabaseExtensions private constructor()

inline fun <reified T : Any> criteria(block: TypedCriteriaBuilder<T>.() -> Unit): Criteria {
    val builder = TypedCriteriaBuilder(T::class)
    builder.block()
    return builder.build()
}