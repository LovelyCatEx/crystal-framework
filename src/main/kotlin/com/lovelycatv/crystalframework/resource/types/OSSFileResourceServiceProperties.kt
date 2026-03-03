package com.lovelycatv.crystalframework.resource.types

data class OSSFileResourceServiceProperties(
    val accessKeyId: String,
    val accessKeySecret: String,
    val securityToken: String,
    val region: String,
    val bucketName: String,
)
