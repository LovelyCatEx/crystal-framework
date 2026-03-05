/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.shared.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


class JSONExtensions private constructor()

val objectMapper: ObjectMapper = jacksonObjectMapper()

fun Any?.toJSONString(objectMapper: ObjectMapper = com.lovelycatv.crystalframework.shared.utils.objectMapper): String {
    return if (this == null) "null" else objectMapper.writeValueAsString(this)
}

inline fun <reified T> String.parseObject(objectMapper: ObjectMapper = com.lovelycatv.crystalframework.shared.utils.objectMapper): T {
    return objectMapper.readValue(this, T::class.java)
}