package com.lovelycatv.crystalframework.cache.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class InvalidateCache(
    val key: String = "",
    val keys: Array<String> = [],
)