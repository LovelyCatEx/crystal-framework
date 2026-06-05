package com.lovelycatv.crystalframework.sdk.encrypt.types

data class EncryptionExclusionDeclaration(
    /**
     * Spring `PathPattern` syntax. Examples:
     *  - `/api/v1/actuator/\*\*` matches the actuator subtree.
     *  - `/api/v1/public/health` matches a single path.
     */
    val pathPattern: String,
    val description: String = "",
)
