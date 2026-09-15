/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.types

data class OSSFileResourceServiceProperties(
    val accessKeyId: String,
    val accessKeySecret: String,
    val securityToken: String,
    val region: String,
    val bucketName: String,
    override val basePath: String = "",
) : FileResourceServiceProperties
