package com.lovelycatv.crystalframework.shared.utils.encrypt

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object RSA {

    private val oaep256Spec = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT
    )

    fun generateKeyPair(): Pair<String, String> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        return Pair(publicKey, privateKey)
    }

    fun encryptWithPublicKey(data: String, publicKeyStr: String): String {
        val publicKey = getPublicKeyFromString(publicKeyStr)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            publicKey,
            oaep256Spec
        )
        val encryptedBytes = cipher.doFinal(
            data.toByteArray(Charsets.UTF_8)
        )
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decryptWithPrivateKey(encryptedData: String, privateKeyStr: String): String {
        val privateKey = getPrivateKeyFromString(privateKeyStr)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            privateKey,
            oaep256Spec
        )
        val decryptedBytes = cipher.doFinal(
            Base64.getDecoder()
                .decode(encryptedData)
        )

        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getPublicKeyFromString(keyStr: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(keyStr)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory
            .getInstance("RSA")
            .generatePublic(keySpec)
    }

    private fun getPrivateKeyFromString(keyStr: String): PrivateKey {
        val keyBytes = Base64.getDecoder().decode(keyStr)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory
            .getInstance("RSA")
            .generatePrivate(keySpec)
    }
}