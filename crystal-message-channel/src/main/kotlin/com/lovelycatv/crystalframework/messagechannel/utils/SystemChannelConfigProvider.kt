package com.lovelycatv.crystalframework.messagechannel.utils

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.EmailChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.LarkChannelConfig
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import org.springframework.stereotype.Component

/**
 * Maps the global [com.lovelycatv.crystalframework.shared.types.system.SystemSettings] block
 * for a given [ChannelType] into a strongly-typed [ChannelConfig].
 *
 * Used when sending a message under the *system* identity (i.e. there is no tenant-level
 * channel record). Tenant-scoped sends should obtain their config from the tenant-side
 * service instead.
 */
@Component
class SystemChannelConfigProvider(
    private val systemModuleClient: SystemModuleClient,
) {
    /** Returns null when system settings are not initialized or the channel block is unset. */
    suspend fun resolveOrNull(channelType: ChannelType): ChannelConfig? {
        val settings = systemModuleClient.getSystemSettings() ?: return null
        return when (channelType) {
            ChannelType.EMAIL -> settings.mail.smtp.let {
                EmailChannelConfig(
                    host = it.host,
                    port = it.port,
                    username = it.username,
                    password = it.password,
                    ssl = it.ssl,
                    fromEmail = it.fromEmail,
                )
            }
            ChannelType.LARK -> settings.messageChannel.lark.let {
                LarkChannelConfig(
                    appId = it.appId,
                    appSecret = it.appSecret,
                    baseUrl = it.baseUrl,
                )
            }
        }
    }

    suspend fun resolve(channelType: ChannelType): ChannelConfig =
        resolveOrNull(channelType)
            ?: error("System settings not initialized; cannot build $channelType channel config")
}
