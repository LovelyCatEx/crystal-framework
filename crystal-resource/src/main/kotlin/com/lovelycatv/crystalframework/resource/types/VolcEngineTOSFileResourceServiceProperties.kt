package com.lovelycatv.crystalframework.resource.types

data class VolcEngineTOSFileResourceServiceProperties(
    val accessKey: String,
    val secretKey: String,
    val region: String,
    val endpoint: String,
    val bucketName: String,
    override val basePath: String = "",
) : FileResourceServiceProperties
