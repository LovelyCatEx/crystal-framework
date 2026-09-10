package com.lovelycatv.crystalframework.ai.controller.manager.provider

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.constants.AiProviderResponseConfigs
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerCreateAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerDeleteAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerReadAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerUpdateAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.vo.DefaultProviderConfigsVO
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiProviderRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-provider")
class AiProviderManagerController(
    managerService: AiProviderManagerService,
) : StandardManagerController<
    AiProviderManagerService,
    AiProviderRepository,
    AiProviderEntity,
    ManagerCreateAiProviderDTO,
    ManagerReadAiProviderDTO,
    ManagerUpdateAiProviderDTO,
    ManagerDeleteAiProviderDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_PROVIDER_CREATE_NAME,
        systemRead = AiPermission.ACTION_SYSTEM_AI_PROVIDER_READ_NAME,
        systemUpdate = AiPermission.ACTION_SYSTEM_AI_PROVIDER_UPDATE_NAME,
        systemDelete = AiPermission.ACTION_SYSTEM_AI_PROVIDER_DELETE_NAME,
    ),
) {
    @GetMapping("/default-configs", version = "1")
    @RequiresAuthority(
        anyOf = [
            AiPermission.ACTION_SYSTEM_AI_PROVIDER_CREATE_NAME,
            AiPermission.ACTION_SYSTEM_AI_PROVIDER_UPDATE_NAME
        ],
        scope = ResourceScope.SYSTEM
    )
    suspend fun getDefaultConfigs(): ApiResponse<DefaultProviderConfigsVO> {
        return ApiResponse.success(
            DefaultProviderConfigsVO(
                openai = AiProviderResponseConfigs.OPENAI,
                anthropic = AiProviderResponseConfigs.ANTHROPIC,
            )
        )
    }
}
