package com.lovelycatv.crystalframework.sdk.config

import com.lovelycatv.crystalframework.sdk.CrystalFrameworkPackageScanRegistry

fun interface CrystalFrameworkPackageScanConfigurer {
    fun configure(registry: CrystalFrameworkPackageScanRegistry)
}
