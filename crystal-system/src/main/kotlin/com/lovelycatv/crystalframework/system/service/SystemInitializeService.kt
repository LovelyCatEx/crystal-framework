/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.service

interface SystemInitializeService {
    suspend fun prepareInitializationToken(): String?

    suspend fun initializeSystem(
        initializationToken: String?,
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