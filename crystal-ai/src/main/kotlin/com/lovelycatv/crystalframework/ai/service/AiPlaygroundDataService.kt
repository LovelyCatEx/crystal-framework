/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundDataVO

/**
 * Resolves the data the AI playground needs to render, narrowed to the user groups the given user
 * belongs to.
 */
interface AiPlaygroundDataService {
    suspend fun getPlaygroundData(userId: Long): ManagerAiPlaygroundDataVO
}
