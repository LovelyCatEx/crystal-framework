/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

/**
 * A provider as shown in the playground: only its display name and the enabled models that hang
 * under it. Credentials, base URL and request/response config are deliberately not exposed.
 */
data class ManagerAiPlaygroundProviderVO(
    val name: String,
    val modelIds: List<String>,
)
