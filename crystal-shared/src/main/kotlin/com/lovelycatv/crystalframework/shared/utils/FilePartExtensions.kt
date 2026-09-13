/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.utils

import org.springframework.http.codec.multipart.FilePart
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*

class FilePartExtensions private constructor()

fun <T: FilePart> T.getContentType(default: String = "application/octet-stream"): String {
    return this.headers().contentType?.toString()
        ?: default
}

suspend fun <T: FilePart> T.asInputStream(): InputStream {
    val (inputStream) = this.asInputStreamWithLength()
    return inputStream
}

suspend fun <T: FilePart> T.asInputStreamWithLength(): Pair<InputStream, Long> {
    val dataBuffers = this.content()
        .awaitListWithTimeout()

    val totalSize = dataBuffers.sumOf { it.readableByteCount().toLong() }

    val streams = dataBuffers.map { it.asInputStream(true) }

    return SequenceInputStream(Collections.enumeration(streams)) to totalSize
}
