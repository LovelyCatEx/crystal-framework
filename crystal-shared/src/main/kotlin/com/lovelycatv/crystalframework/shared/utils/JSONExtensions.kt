/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


class JSONExtensions private constructor()

val objectMapper: ObjectMapper = jacksonObjectMapper()

fun Any?.toJSONString(objectMapper: ObjectMapper = com.lovelycatv.crystalframework.shared.utils.objectMapper): String {
    return if (this == null) "null" else objectMapper.writeValueAsString(this)
}

fun Any?.toPrettierJSONString(objectMapper: ObjectMapper = com.lovelycatv.crystalframework.shared.utils.objectMapper): String {
    return if (this == null) "null" else objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

inline fun <reified T> String.parseObject(objectMapper: ObjectMapper = com.lovelycatv.crystalframework.shared.utils.objectMapper): T {
    return objectMapper.readValue(this, T::class.java)
}