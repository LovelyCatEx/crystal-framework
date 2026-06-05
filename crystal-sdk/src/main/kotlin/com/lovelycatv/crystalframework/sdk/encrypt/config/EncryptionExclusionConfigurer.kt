package com.lovelycatv.crystalframework.sdk.encrypt.config

import com.lovelycatv.crystalframework.sdk.encrypt.EncryptionExclusionRegistry

fun interface EncryptionExclusionConfigurer {
    fun configure(registry: EncryptionExclusionRegistry)
}
