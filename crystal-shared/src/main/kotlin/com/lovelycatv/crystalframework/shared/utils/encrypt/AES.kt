package com.lovelycatv.crystalframework.shared.utils.encrypt

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES {
    fun generateSecretKey(): String {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }

    fun encryptWithAES(data: String, secretKeyStr: String): String {
        val secretKey = getSecretKeyFromString(secretKeyStr)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val gcMParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcMParameterSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val encryptedDataWithIv = iv + encryptedBytes
        return Base64.getEncoder().encodeToString(encryptedDataWithIv)
    }

    fun decryptWithAES(encryptedDataStr: String, secretKeyStr: String): String {
        val secretKey = getSecretKeyFromString(secretKeyStr)
        val encryptedDataWithIv = Base64.getDecoder().decode(encryptedDataStr)
        val iv = encryptedDataWithIv.copyOfRange(0, 12)
        val encryptedData = encryptedDataWithIv.copyOfRange(12, encryptedDataWithIv.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcMParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcMParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedData)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getSecretKeyFromString(keyStr: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(keyStr)
        return SecretKeySpec(keyBytes, "AES")
    }
}