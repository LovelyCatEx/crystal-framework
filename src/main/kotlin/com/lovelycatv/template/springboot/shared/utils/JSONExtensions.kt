/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.template.springboot.shared.utils

import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

class JSONExtensions private constructor()

val objectMapper: ObjectMapper = jacksonObjectMapper()

fun Any?.toJSONString(objectMapper: ObjectMapper = com.lovelycatv.template.springboot.shared.utils.objectMapper)
        = if (this == null) "null" else objectMapper.writeValueAsString(this)

inline fun <reified T> String.parseObject(objectMapper: ObjectMapper = com.lovelycatv.template.springboot.shared.utils.objectMapper): T {
    return objectMapper.readValue(this, T::class.java)
}