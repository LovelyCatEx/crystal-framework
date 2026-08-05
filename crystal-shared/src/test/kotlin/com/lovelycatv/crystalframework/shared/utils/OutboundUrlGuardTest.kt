package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OutboundUrlGuardTest {

    private val larkAllowlist = listOf("*.feishu.cn")

    // region check 1: scheme / userinfo / host presence

    @Test
    fun `rejects non-https scheme`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("http://open.feishu.cn/api", larkAllowlist)
        }
    }

    @Test
    fun `rejects non-http schemes such as file and gopher`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("file:///etc/passwd", listOf("*"))
        }
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("gopher://open.feishu.cn/", larkAllowlist)
        }
    }

    @Test
    fun `rejects url carrying userinfo even when host is allowed`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://evil.com@open.feishu.cn/api", larkAllowlist)
        }
    }

    @Test
    fun `rejects malformed url`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("ht tp://bad url", listOf("*"))
        }
    }

    // endregion
    // region check 2: private / loopback / reserved IP literals

    @Test
    fun `rejects loopback ip literal`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://127.0.0.1/api", listOf("*"))
        }
    }

    @Test
    fun `rejects private ranges and cloud metadata endpoint`() {
        listOf(
            "https://10.0.0.5/",
            "https://192.168.1.1/",
            "https://172.16.0.1/",
            "https://169.254.169.254/latest/meta-data",
            "https://[::1]/",
        ).forEach { url ->
            assertThrows(BusinessException::class.java) {
                OutboundUrlGuard.assertAllowed(url, listOf("*"))
            }
        }
    }

    @Test
    fun `star wildcard still blocks private ip literal`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://127.0.0.1/", listOf("*"))
        }
    }

    // endregion
    // region check 3: host allowlist matching

    @Test
    fun `subdomain wildcard matches domain itself and its subdomains`() {
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://feishu.cn/api", larkAllowlist)
        }
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://open.feishu.cn/api", larkAllowlist)
        }
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://a.b.feishu.cn/api", larkAllowlist)
        }
    }

    @Test
    fun `bare entry matches only the exact host not its subdomains`() {
        val bare = listOf("feishu.com")
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://feishu.com/api", bare)
        }
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://api.feishu.com/api", bare)
        }
    }

    @Test
    fun `wildcard does not match a different top level domain`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://open.feishu.com/api", larkAllowlist)
        }
    }

    @Test
    fun `look-alike host sharing the suffix text is rejected`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://evilfeishu.cn/api", larkAllowlist)
        }
    }

    @Test
    fun `tld wildcard matches any host under that suffix`() {
        val allowlist = listOf("*.net")
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://a.net/", allowlist)
        }
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://b.a.net/", allowlist)
        }
    }

    @Test
    fun `star wildcard allows any public host`() {
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://anything.example.org/x", listOf("*"))
        }
    }

    @Test
    fun `host not in allowlist is rejected`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://evil.com/api", larkAllowlist)
        }
    }

    @Test
    fun `matching is case-insensitive and tolerates surrounding whitespace in entries`() {
        assertDoesNotThrow {
            OutboundUrlGuard.assertAllowed("https://OPEN.FEISHU.CN/api", listOf("  *.FEISHU.CN  "))
        }
    }

    @Test
    fun `blank and empty allowlist entries are ignored`() {
        assertThrows(BusinessException::class.java) {
            OutboundUrlGuard.assertAllowed("https://open.feishu.cn/api", listOf("", "   "))
        }
    }

    // endregion
}
