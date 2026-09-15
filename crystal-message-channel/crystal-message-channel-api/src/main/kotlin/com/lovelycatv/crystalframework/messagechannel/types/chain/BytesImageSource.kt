/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * In-memory image bytes. Not serializable to XML (the XML form only supports url/resource sources).
 */
class BytesImageSource(
    val bytes: ByteArray,
    val filename: String? = null,
    val contentType: String? = null,
) : ImageSource
