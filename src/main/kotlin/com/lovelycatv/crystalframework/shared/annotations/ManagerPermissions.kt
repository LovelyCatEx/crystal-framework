package com.lovelycatv.crystalframework.shared.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagerPermissions(
    val read: String = "",
    val readAll: String = "",
    val create: String = "",
    val update: String = "",
    val delete: String = ""
)