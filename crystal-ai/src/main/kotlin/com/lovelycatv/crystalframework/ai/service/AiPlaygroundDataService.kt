package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundDataVO

/**
 * Resolves the data the AI playground needs to render, narrowed to the user groups the given user
 * belongs to.
 */
interface AiPlaygroundDataService {
    suspend fun getPlaygroundData(userId: Long): ManagerAiPlaygroundDataVO
}
