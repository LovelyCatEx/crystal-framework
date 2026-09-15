/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.encrypt.types

data class EncryptionExclusionDeclaration(
    /**
     * Spring `PathPattern` syntax. Examples:
     *  - `/api/v1/public/health` matches a single path.
     */
    val pathPattern: String,
    val description: String = "",
)
