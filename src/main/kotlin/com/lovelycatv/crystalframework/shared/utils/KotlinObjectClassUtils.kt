package com.lovelycatv.crystalframework.shared.utils

import kotlin.reflect.full.memberProperties

object KotlinObjectClassUtils {
    inline fun <reified T> extractAllValProperties(
        instance: Any,
        includingNestedObjectClass: Boolean
    ): List<T> {
        val results = mutableListOf<T>()

        instance::class.memberProperties
            .forEach { property ->
                if (property.isConst) {
                    val value = property.getter.call()
                    if (value is T) {
                        results.add(value)
                    }
                } else {
                    val value = property.getter.call(instance)
                    if (value is T) {
                        results.add(value)
                    }
                }
            }

        if (includingNestedObjectClass) {
            var nested = instance::class.nestedClasses
            while (nested.isNotEmpty()) {
                nested.forEach {
                    val instance = it.objectInstance
                    it.memberProperties.forEach { prop ->
                        if (prop.isConst) {
                            val value = prop.getter.call()
                            if (value is T) {
                                results.add(value)
                            }
                        } else {
                            val value = if (instance != null) {
                                prop.call(instance)
                            } else {
                                prop.getter.call()
                            }

                            if (value is T) {
                                results.add(value)
                            }
                        }
                    }
                }

                nested = nested.flatMap { it.nestedClasses }
            }
        }

        return results
    }
}