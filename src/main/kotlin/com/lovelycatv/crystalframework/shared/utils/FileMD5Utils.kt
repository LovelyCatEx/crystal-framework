package com.lovelycatv.crystalframework.shared.utils

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

object FileMD5Utils {
    fun calculateMD5(inputStream: InputStream): String {
        return inputStream.use { inputStream ->
            val md = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }

            md.digest().joinToString("") { "%02x".format(it) }
        }
    }

    fun calculateMD5(file: File): String {
        return file.inputStream().use {
            calculateMD5(it)
        }
    }
}