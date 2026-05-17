package com.lovelycatv.crystalframework.shared.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class InvalidateCache(
    val key: String = "",
    val keys: Array<String> = [],
)