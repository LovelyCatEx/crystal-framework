package com.lovelycatv.crystalframework.shared.database

import org.springframework.data.relational.core.mapping.Column
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

object EntityColumnCache {
    private val cache = ConcurrentHashMap<KClass<*>, Map<KProperty1<*, *>, String>>()
    
    fun getColumnMap(entityClass: KClass<*>): Map<KProperty1<*, *>, String> {
        @Suppress("UNCHECKED_CAST")
        return cache.getOrPut(entityClass) {
            entityClass.memberProperties.associate { property ->
                val columnName = property.findAnnotation<Column>()?.value
                    ?: property.name
                property as KProperty1<*, *> to columnName
            }
        }
    }
    
    fun <T : Any> getColumnName(property: KProperty1<T, *>): String {
        val entityClass = getEntityClass(property)
            ?: throw IllegalStateException("Unable to find column name of property ${property.name}")

        return getColumnMap(entityClass)
            .getOrElse(property) { property.name }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getEntityClass(property: KProperty1<T, *>): KClass<T>? {
        val declaringClass = property.javaField?.declaringClass?.kotlin
        return if (declaringClass != null) {
            declaringClass as KClass<T>
        } else {
            null
        }
    }
}
