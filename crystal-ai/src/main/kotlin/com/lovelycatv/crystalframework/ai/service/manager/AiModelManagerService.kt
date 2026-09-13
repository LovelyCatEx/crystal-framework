/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerCreateAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerDeleteAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerReadAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerUpdateAiModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiModelManagerService : CachedBaseManagerService<
    AiModelRepository,
    AiModelEntity,
    ManagerCreateAiModelDTO,
    ManagerReadAiModelDTO,
    ManagerUpdateAiModelDTO,
    ManagerDeleteAiModelDTO
>
