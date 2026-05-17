package com.lovelycatv.crystalframework.system.service

interface SystemInitializeService {
    suspend fun initializeSystem(
        username: String,
        password: String,
        email: String,
        smtpHost: String,
        smtpPort: Int,
        smtpUsername: String,
        smtpPassword: String,
        fromEmail: String,
        fromName: String
    )

    suspend fun isSystemInitialized(): Boolean
}