package com.lovelycatv.crystalframework.spi.message.config

import com.lovelycatv.crystalframework.sdk.message.AudienceResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.PartyResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.ScopeResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver
import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.config.MessageScopeResolver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Assembles the messaging scope / party resolver registries from every resolver
 * bean on the classpath. Mirrors [com.lovelycatv.crystalframework.config.EncryptionExclusionRegistryConfiguration]:
 * concrete resolvers are ordinary `@Component` beans, collected here once at
 * startup. The messaging core never depends on any concrete resolver.
 */
@Configuration
class MessageResolverRegistryConfiguration {
    @Bean
    fun scopeResolverRegistry(resolvers: ObjectProvider<MessageScopeResolver>): ScopeResolverRegistry =
        ScopeResolverRegistry(resolvers.orderedStream().toList())

    @Bean
    fun partyResolverRegistry(resolvers: ObjectProvider<MessagePartyResolver>): PartyResolverRegistry =
        PartyResolverRegistry(resolvers.orderedStream().toList())

    @Bean
    fun audienceResolverRegistry(resolvers: ObjectProvider<MessageAudienceResolver>): AudienceResolverRegistry =
        AudienceResolverRegistry(resolvers.orderedStream().toList())
}