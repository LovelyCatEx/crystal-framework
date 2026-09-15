/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service.impl

import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundDataVO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundGroupVO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundModelVO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundProviderVO
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupMemberRepository
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.ai.service.AiPlaygroundDataService
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class AiPlaygroundDataServiceImpl(
    private val aiUserGroupMemberRepository: AiUserGroupMemberRepository,
    private val aiUserGroupModelRepository: AiUserGroupModelRepository,
    private val aiModelManagerService: AiModelManagerService,
    private val aiProviderManagerService: AiProviderManagerService,
    private val aiUserGroupManagerService: AiUserGroupManagerService,
) : AiPlaygroundDataService {

    override suspend fun getPlaygroundData(userId: Long): ManagerAiPlaygroundDataVO {
        val userGroupIds = aiUserGroupMemberRepository.findAllByUserId(userId)
            .collectList().awaitFirstOrNull().orEmpty()
            .map { it.userGroupId }
            .distinct()

        // groupId -> modelIds (raw, before filtering to enabled models)
        val groupModelIds = userGroupIds.associateWith { groupId ->
            aiUserGroupModelRepository.findAllByUserGroupId(groupId)
                .collectList().awaitFirstOrNull().orEmpty()
                .map { it.modelId }
        }

        val models = aiModelManagerService.resolveEnabledModels(groupModelIds.values.flatten().distinct())
        val providers = aiProviderManagerService.resolveProviders(models.values.map { it.providerId }.distinct())
        val groups = aiUserGroupManagerService.resolveGroups(userGroupIds)

        val modelVos = models.entries.associate { (id, model) -> id.toString() to model.toVO() }

        val providerVos = providers.entries.associate { (id, provider) ->
            id.toString() to ManagerAiPlaygroundProviderVO(
                name = provider.name,
                modelIds = models.values
                    .filter { it.providerId == id }
                    .map { it.id.toString() },
            )
        }

        val groupVos = groups.entries.associate { (id, group) ->
            id.toString() to ManagerAiPlaygroundGroupVO(
                name = group.name,
                billingMultiplier = group.billingMultiplier,
                modelIds = groupModelIds[id].orEmpty()
                    .filter { models.containsKey(it) }
                    .map { it.toString() },
            )
        }

        return ManagerAiPlaygroundDataVO(
            providers = providerVos,
            groups = groupVos,
            models = modelVos,
        )
    }

    private suspend fun AiModelManagerService.resolveEnabledModels(ids: List<Long>): Map<Long, AiModelEntity> {
        return ids.mapNotNull { id ->
            getByIdOrNull(id)?.takeIf { it.enabled }?.let { id to it }
        }.toMap()
    }

    private suspend fun AiProviderManagerService.resolveProviders(ids: List<Long>): Map<Long, AiProviderEntity> {
        return ids.mapNotNull { id ->
            getByIdOrNull(id)?.let { id to it }
        }.toMap()
    }

    private suspend fun AiUserGroupManagerService.resolveGroups(ids: List<Long>): Map<Long, AiUserGroupEntity> {
        return ids.mapNotNull { id ->
            getByIdOrNull(id)?.takeIf { it.enabled }?.let { id to it }
        }.toMap()
    }

    private fun AiModelEntity.toVO() = ManagerAiPlaygroundModelVO(
        displayName = displayName,
        key = key,
        inputPricePerMillion = inputPricePerMillion,
        outputPricePerMillion = outputPricePerMillion,
        cacheReadPricePerMillion = cacheReadPricePerMillion,
        cacheWritePricePerMillion = cacheWritePricePerMillion,
        capabilities = getRealCapabilities().map { it.typeId }.toSet(),
        contextWindowTokens = contextWindowTokens,
        currency = currency,
    )
}
