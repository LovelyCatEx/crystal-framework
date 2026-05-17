package com.lovelycatv.crystalframework.shared.config

import io.r2dbc.spi.Connection
import io.r2dbc.spi.Statement

class DelegatedR2dbcConnection(private val delegate: Connection) : Connection by delegate {
    override fun createStatement(p0: String): Statement {
        return delegate.createStatement(CrystalFrameworkSQLModifier.processSql(p0))
    }
}