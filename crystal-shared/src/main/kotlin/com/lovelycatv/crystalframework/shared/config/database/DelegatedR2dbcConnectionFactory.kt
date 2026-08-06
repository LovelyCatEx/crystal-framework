package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

class DelegatedR2dbcConnectionFactory(
    private val delegate: ConnectionFactory,
) : ConnectionFactory {
    override fun create(): Publisher<out Connection> {
        return Mono.from(delegate.create())
            .map(::DelegatedR2dbcConnection)
    }

    override fun getMetadata(): ConnectionFactoryMetadata {
        return delegate.metadata
    }
}
