package com.lovelycatv.crystalframework.resource.utils

import org.apache.tika.Tika

// Tika is thread-safe and expensive to construct — one instance for the whole module
private val TIKA = Tika()

/**
 * Detects the actual MIME type of [bytes] using Apache Tika magic-number analysis.
 * Only the file header is inspected; the full byte array may be passed safely.
 * Never trust a client-provided Content-Type header — always call this instead.
 */
fun detectMimeType(bytes: ByteArray): String = TIKA.detect(bytes)
