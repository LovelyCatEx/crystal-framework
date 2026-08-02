package com.lovelycatv.crystalframework.resource.utils

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Signs and verifies short-lived download URLs for non-public local file resources.
 *
 * The signature is an HMAC-SHA256 over `fileId|exp` (hex encoded). Because `<img src>` cannot
 * carry an Authorization header, access to a non-public local file is proven by a time-limited
 * signature minted only after [com.lovelycatv.crystalframework.resource.service.ResourceAccessService]
 * has authorized the viewer at URL-generation time.
 */
@Component
class ResourceUrlSigner(
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    private val logger = logger()

    private val signingKey: String
        get() = crystalFrameworkConfiguration.resource.signingKey

    @PostConstruct
    fun validateKey() {
        check(signingKey.isNotBlank()) {
            "crystalframework.resource.signing-key must be set; refusing to start with blank key"
        }
        logger.info("ResourceUrlSigner initialized with a valid signing key")
    }

    /**
     * @param fileId the file resource id
     * @param expireAt epoch millis after which the signature is no longer valid
     * @return hex-encoded HMAC-SHA256 signature
     */
    fun sign(fileId: Long, expireAt: Long): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(signingKey.toByteArray(), HMAC_ALGORITHM))
        return mac.doFinal(payload(fileId, expireAt).toByteArray()).toHex()
    }

    /**
     * @return true only when [signature] matches and [expireAt] is still in the future.
     */
    fun verify(fileId: Long, expireAt: Long, signature: String): Boolean {
        if (System.currentTimeMillis() > expireAt) {
            return false
        }
        val expected = sign(fileId, expireAt)
        return MessageDigest.isEqual(expected.toByteArray(), signature.toByteArray())
    }

    private fun payload(fileId: Long, expireAt: Long): String = "$fileId|$expireAt"

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
    }
}
