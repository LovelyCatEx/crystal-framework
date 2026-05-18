package com.lovelycatv.crystalframework.shared.utils.encrypt

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object RSA {

    private val spec = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec("SHA-256"),
        PSource.PSpecified.DEFAULT
    )

    fun generateKeyPair(): Pair<String, String> {
        val generator =
            KeyPairGenerator.getInstance("RSA")

        generator.initialize(2048)

        val pair =
            generator.generateKeyPair()

        val publicKey =
            Base64.getEncoder()
                .encodeToString(pair.public.encoded)

        val privateKey =
            Base64.getEncoder()
                .encodeToString(pair.private.encoded)

        return Pair(publicKey, privateKey)
    }

    fun encryptWithPublicKey(
        data: String,
        publicKeyStr: String
    ): String {

        val publicKey =
            getPublicKeyFromString(publicKeyStr)

        val cipher = Cipher.getInstance(
            "RSA/ECB/OAEPPadding"
        )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            publicKey,
            spec
        )

        val encrypted =
            cipher.doFinal(
                data.toByteArray(Charsets.UTF_8)
            )

        return Base64.getEncoder()
            .encodeToString(encrypted)
    }

    fun decryptWithPrivateKey(
        encryptedData: String,
        privateKeyStr: String
    ): String {

        val privateKey =
            getPrivateKeyFromString(privateKeyStr)

        val cipher = Cipher.getInstance(
            "RSA/ECB/OAEPPadding"
        )

        cipher.init(
            Cipher.DECRYPT_MODE,
            privateKey,
            spec
        )

        val decrypted =
            cipher.doFinal(
                Base64.getDecoder()
                    .decode(encryptedData)
            )

        return String(
            decrypted,
            Charsets.UTF_8
        )
    }

    private fun getPublicKeyFromString(
        keyStr: String
    ): PublicKey {

        return KeyFactory.getInstance("RSA")
            .generatePublic(
                X509EncodedKeySpec(
                    Base64.getDecoder()
                        .decode(keyStr)
                )
            )
    }

    private fun getPrivateKeyFromString(
        keyStr: String
    ): PrivateKey {

        return KeyFactory.getInstance("RSA")
            .generatePrivate(
                PKCS8EncodedKeySpec(
                    Base64.getDecoder()
                        .decode(keyStr)
                )
            )
    }
}