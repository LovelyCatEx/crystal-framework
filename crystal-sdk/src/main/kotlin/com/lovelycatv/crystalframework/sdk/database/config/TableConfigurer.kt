package com.lovelycatv.crystalframework.sdk.database.config

import com.lovelycatv.crystalframework.sdk.database.TableRegistry

fun interface TableConfigurer {
    fun configure(registry: TableRegistry)
}
