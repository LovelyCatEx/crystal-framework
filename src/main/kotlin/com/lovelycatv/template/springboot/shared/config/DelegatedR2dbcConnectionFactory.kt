package com.lovelycatv.template.springboot.shared.config

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DelegatedR2dbcConnectionFactory(private val delegate: ConnectionFactory) : ConnectionFactory by delegate {
    override fun create(): Publisher<out Connection> {
        return when (val created = delegate.create()) {
            is Mono<out Connection> -> {
                created.map { DelegatedR2dbcConnection(it) }
            }

            is Flux<out Connection> -> {
                created.map { DelegatedR2dbcConnection(it) }
            }

            else -> {
                throw IllegalStateException("Unsupported publisher type ${created::class.qualifiedName} when creating connection")
            }
        }
    }
}