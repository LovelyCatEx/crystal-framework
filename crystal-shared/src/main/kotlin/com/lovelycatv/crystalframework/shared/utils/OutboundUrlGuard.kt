package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import java.net.InetAddress
import java.net.URI

/**
 * SSRF guard for outbound requests whose target URL is (partly) user/tenant-configurable.
 *
 * The guard is intentionally allowlist-first and does NOT perform DNS resolution or follow
 * redirects — callers that need rebinding / redirect protection must add it separately. It
 * enforces three cheap, deterministic checks against a caller-supplied host-suffix allowlist:
 *
 * 1. Scheme must be HTTPS (rejects `http`, `file`, `gopher`, embedded `userinfo`, ...).
 * 2. If the host is an IP literal, loopback / private / link-local / reserved ranges are rejected
 *    (blocks the obvious `https://127.0.0.1`, `https://169.254.169.254`, `https://10.x` payloads).
 * 3. The host must match one of [allowedHostSuffixes]. Only wildcard entries match subdomains:
 *    `*` (any host), `*.example.com` (that domain and its subdomains), `*.net` (any host under the
 *    `net` suffix). A bare entry like `example.com` matches ONLY that exact host — subdomains such
 *    as `api.example.com` require an explicit `*.example.com`. Note: `*` only disables the host
 *    allowlist — checks 1 and 2 still apply.
 *
 * The allowlist is supplied by the caller (typically from system settings) so this utility stays
 * free of any business dependency and is reusable by every outbound integration.
 */
object OutboundUrlGuard {
    /**
     * Validates [rawUrl] against [allowedHostSuffixes]. Throws [BusinessException] when the URL is
     * malformed, uses a non-HTTPS scheme, points at a private/loopback IP literal, or its host is
     * not covered by the allowlist. Returns normally when the URL is allowed.
     *
     * @param allowedHostSuffixes host suffixes such as `feishu.cn`; `open.feishu.cn` and the bare
     *   `feishu.cn` both match, `evilfeishu.cn` does not.
     */
    fun assertAllowed(rawUrl: String, allowedHostSuffixes: List<String>) {
        val uri = try {
            URI(rawUrl.trim())
        } catch (e: Exception) {
            throw BusinessException("Outbound URL is malformed: ${e.message}")
        }

        val scheme = uri.scheme?.lowercase()
        if (scheme != SCHEME_HTTPS) {
            throw BusinessException("Outbound URL must use HTTPS, got scheme '${uri.scheme}'")
        }

        if (uri.userInfo != null) {
            throw BusinessException("Outbound URL must not contain userinfo")
        }

        val host = uri.host?.lowercase()
            ?: throw BusinessException("Outbound URL host is missing")

        assertHostAllowed(host, allowedHostSuffixes)
    }

    /**
     * Validates a bare host (no scheme, e.g. an SMTP server host such as `smtp.example.com`) against
     * [allowedHostSuffixes]. Runs the two scheme-agnostic checks only — private/loopback IP literal
     * rejection and allowlist matching — deliberately WITHOUT the HTTPS check, because non-HTTP
     * outbound protocols (SMTP over TLS on 465/587, ...) are not HTTPS. Throws [BusinessException]
     * when the host is blank, an IP literal in a non-routable range, or not covered by the allowlist.
     *
     * @param host bare host name or IP literal; case-insensitive
     * @param allowedHostSuffixes host suffixes, same matching semantics as [assertAllowed]
     */
    fun assertHostAllowed(host: String, allowedHostSuffixes: List<String>) {
        val normalizedHost = host.trim().lowercase()
        if (normalizedHost.isEmpty()) {
            throw BusinessException("Outbound host is missing")
        }

        assertNotPrivateIpLiteral(normalizedHost)

        val matched = allowedHostSuffixes
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .any { pattern -> hostMatchesPattern(normalizedHost, pattern) }
        if (!matched) {
            throw BusinessException("Outbound host '$normalizedHost' is not in the allowed hosts list")
        }
    }

    /**
     * Matches [host] against a single allowlist [pattern]. Only wildcard patterns match subdomains;
     * a bare host matches that exact host and nothing else.
     *
     * - `*`             — matches any host (host allowlist disabled; the HTTPS and private-IP-literal
     *                     checks in [assertAllowed] still apply, so loopback/intranet literals stay blocked).
     * - `*.example.com` — matches `example.com` and any of its subdomains (e.g. `api.example.com`).
     * - `*.net`         — matches any host under the `net` suffix (e.g. `a.net`, `b.a.net`).
     * - `example.com`   — bare form, matches ONLY `example.com`; `api.example.com` does NOT match.
     */
    private fun hostMatchesPattern(host: String, pattern: String): Boolean {
        if (pattern == WILDCARD_ALL) return true
        if (pattern.startsWith(WILDCARD_SUBDOMAIN_PREFIX)) {
            val suffix = pattern.removePrefix(WILDCARD_SUBDOMAIN_PREFIX)
            if (suffix.isEmpty()) return false
            return host == suffix || host.endsWith(".$suffix")
        }
        return host == pattern
    }

    /**
     * Rejects the host when it is an IP literal in a non-routable range. Non-IP hosts (domain names)
     * pass through untouched — this guard deliberately does not resolve DNS.
     */
    private fun assertNotPrivateIpLiteral(host: String) {
        val literal = host.removePrefix("[").removeSuffix("]")
        val address = parseIpLiteralOrNull(literal) ?: return
        if (address.isLoopbackAddress ||
            address.isAnyLocalAddress ||
            address.isLinkLocalAddress ||
            address.isSiteLocalAddress ||
            address.isMulticastAddress
        ) {
            throw BusinessException("Outbound URL host '$host' resolves to a non-routable address")
        }
    }

    /**
     * Returns the [InetAddress] only when [value] is a numeric IP literal. A domain name returns
     * null instead of triggering a DNS lookup, because [InetAddress.getByName] would resolve names.
     */
    private fun parseIpLiteralOrNull(value: String): InetAddress? {
        val looksNumeric = value.contains(':') || value.split(".").let { parts ->
            parts.size == 4 && parts.all { it.toIntOrNull() in 0..255 }
        }
        if (!looksNumeric) return null
        return try {
            InetAddress.getByName(value)
        } catch (_: Exception) {
            null
        }
    }

    private const val SCHEME_HTTPS = "https"
    private const val WILDCARD_ALL = "*"
    private const val WILDCARD_SUBDOMAIN_PREFIX = "*."
}
