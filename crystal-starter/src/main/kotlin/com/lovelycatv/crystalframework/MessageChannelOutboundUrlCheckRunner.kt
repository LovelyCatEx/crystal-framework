package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.entity.MessageChannelEntity
import com.lovelycatv.crystalframework.messagechannel.service.manager.MessageChannelManagerService
import com.lovelycatv.crystalframework.messagechannel.types.config.EmailChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.LarkChannelConfig
import com.lovelycatv.crystalframework.messagechannel.utils.ChannelConfigCodec
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.utils.OutboundUrlGuard
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Startup audit for message channels that were persisted BEFORE the outbound-URL allowlist was
 * enforced on the write path ([MessageChannelManagerService] validates create/update, but existing
 * rows never passed through it). Scans every Lark channel and re-runs [OutboundUrlGuard] against
 * its stored `baseUrl`, logging any row that would now be rejected so operators can remediate.
 *
 * This runner is intentionally read-only: it never disables or rewrites a channel, and never fails
 * startup — a bad row is surfaced as a WARN, not an abort. The actual outbound protection is the
 * write-path guard plus (recommended) a use-site guard in the Lark client; this is a visibility aid.
 */
@Order(6)
@Component
class MessageChannelOutboundUrlCheckRunner(
    private val messageChannelManagerService: MessageChannelManagerService,
    private val channelConfigCodec: ChannelConfigCodec,
    private val systemModuleClient: SystemModuleClient,
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val outbound = systemModuleClient.getSystemSettings()?.security?.outbound
        if (outbound == null) {
            logger.warn("System settings unavailable, skipping message channel outbound target audit")
            return
        }

        val channels = runBlocking(Dispatchers.IO) {
            messageChannelManagerService.getRepository()
                .findAll()
                .awaitListWithTimeout()
        }

        if (channels.isEmpty()) return

        var violations = 0
        channels.forEach { channel ->
            if (auditChannel(channel, outbound)) violations++
        }

        if (violations > 0) {
            logger.warn("Message channel outbound target audit finished: $violations channel(s) violate the allowlist")
        }
    }

    /**
     * Re-runs the write-path outbound guard against one stored channel, dispatching by channel type:
     * Lark's HTTPS `baseUrl` against `allowedHosts`, Email/SMTP's bare `host` against
     * `allowedSmtpHosts`. Returns true when the row violates its allowlist (already logged). A row
     * whose config cannot be decoded, or whose type carries no outbound target, is skipped.
     */
    private fun auditChannel(channel: MessageChannelEntity, outbound: SystemSettings.Security.Outbound): Boolean {
        val channelType = channel.getRealChannelType()
        val config = try {
            channelConfigCodec.decode(channel.config, channelType)
        } catch (e: Exception) {
            logger.warn("Message channel id=${channel.id} name='${channel.name}' config could not be decoded for audit: ${e.message}")
            return false
        }
        return try {
            when (config) {
                is LarkChannelConfig -> OutboundUrlGuard.assertAllowed(config.baseUrl, outbound.allowedHosts)
                is EmailChannelConfig -> OutboundUrlGuard.assertHostAllowed(config.host, outbound.allowedSmtpHosts)
            }
            false
        } catch (e: Exception) {
            logger.warn(
                "Message channel id=${channel.id} name='${channel.name}' has an outbound target that " +
                    "violates the allowlist: ${e.message}. This channel predates the write-path guard " +
                    "and should be reviewed/updated."
            )
            true
        }
    }
}
