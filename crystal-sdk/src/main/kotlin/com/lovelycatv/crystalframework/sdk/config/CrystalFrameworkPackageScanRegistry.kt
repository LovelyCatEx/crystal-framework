package com.lovelycatv.crystalframework.sdk.config

import kotlin.reflect.KClass

class CrystalFrameworkPackageScanRegistry {
    private val componentPackages = linkedSetOf<String>()
    private val entityPackages = linkedSetOf<String>()

    fun scanBasePackage(type: KClass<*>) {
        scanBasePackage(type.java)
    }

    fun scanBasePackage(type: Class<*>) {
        scanBasePackage(type.packageName)
    }

    fun scanBasePackage(packageName: String) {
        addPackage(componentPackages, packageName)
    }

    fun scanEntityPackage(type: KClass<*>) {
        scanEntityPackage(type.java)
    }

    fun scanEntityPackage(type: Class<*>) {
        scanEntityPackage(type.packageName)
    }

    fun scanEntityPackage(packageName: String) {
        addPackage(entityPackages, packageName)
    }

    fun componentPackages(): List<String> = componentPackages.toList()

    fun entityPackages(): List<String> = entityPackages.toList()

    private fun addPackage(target: MutableSet<String>, packageName: String) {
        val normalized = packageName.trim()
        if (normalized.isNotBlank()) {
            target.add(normalized)
        }
    }
}