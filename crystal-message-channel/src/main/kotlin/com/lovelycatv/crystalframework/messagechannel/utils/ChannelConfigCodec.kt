package com.lovelycatv.crystalframework.messagechannel.utils

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.EmailChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.LarkChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.SensitiveField
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.utils.encrypt.AES
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import java.util.Base64
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Serializes/deserializes [ChannelConfig] subclasses to/from the JSON string used to persist a
 * channel record (e.g. the `config` column of `tenant_message_channels`).
 *
 * Sensitive fields (annotated with [SensitiveField]) are AES-encrypted and prefixed with
 * [ENC_PREFIX] in the persisted JSON so the resulting blob is human-distinguishable
 * (encrypted vs. plain) when inspecting a database row.
 */
@Component
class ChannelConfigCodec(
    private val objectMapper: ObjectMapper,
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    private val logger = logger()

    private val aesKey: String
        get() = crystalFrameworkConfiguration.messageChannel.encryptionKey

    @PostConstruct
    fun validateKey() {
        check(aesKey.isNotBlank()) {
            "crystalframework.message-channel.encryption-key must be set; refusing to start with blank key"
        }
        try {
            val keyBytes = Base64.getDecoder().decode(aesKey)
            check(keyBytes.size in setOf(16, 24, 32)) {
                "crystalframework.message-channel.encryption-key must decode to 16/24/32 raw bytes, got ${keyBytes.size}"
            }
        } catch (e: IllegalArgumentException) {
            error("crystalframework.message-channel.encryption-key is not valid Base64: ${e.message}")
        }
        logger.info("ChannelConfigCodec initialized with a valid AES key")
    }

    fun encode(config: ChannelConfig): String {
        val node = objectMapper.valueToTree<ObjectNode>(config)
        val sensitive = sensitiveFieldNames(config::class)
        for (field in sensitive) {
            val plain = node.get(field)?.takeIf { !it.isNull }?.asString() ?: continue
            val cipher = ENC_PREFIX + AES.encryptWithAES(plain, aesKey)
            node.put(field, cipher)
        }
        return objectMapper.writeValueAsString(node)
    }

    fun decode(json: String, channelType: ChannelType): ChannelConfig {
        val targetClass = configClassFor(channelType)
        val node = objectMapper.readTree(json) as? ObjectNode
            ?: error("Channel config JSON is not an object: $json")
        val sensitive = sensitiveFieldNames(targetClass)
        for (field in sensitive) {
            val raw = node.get(field)?.takeIf { !it.isNull }?.asString() ?: continue
            val plain = if (raw.startsWith(ENC_PREFIX)) {
                AES.decryptWithAES(raw.removePrefix(ENC_PREFIX), aesKey)
            } else {
                // Tolerate legacy / hand-written rows that stored the value in plain text.
                raw
            }
            node.put(field, plain)
        }
        return objectMapper.treeToValue(node, targetClass.java)
    }

    /**
     * Parses an *unencrypted* JSON object string into a strongly-typed [ChannelConfig].
     * Use this for inbound payloads from the manager UI before persisting.
     */
    fun fromPlainJson(json: String, channelType: ChannelType): ChannelConfig {
        val targetClass = configClassFor(channelType)
        return objectMapper.readValue(json, targetClass.java)
    }

    private fun configClassFor(channelType: ChannelType): KClass<out ChannelConfig> = when (channelType) {
        ChannelType.EMAIL -> EmailChannelConfig::class
        ChannelType.LARK -> LarkChannelConfig::class
    }

    private fun sensitiveFieldNames(clazz: KClass<out ChannelConfig>): List<String> =
        clazz.declaredMemberProperties
            .filter { it.findAnnotation<SensitiveField>() != null }
            .map { it.name }

    companion object {
        const val ENC_PREFIX = "ENC:"
    }
}
